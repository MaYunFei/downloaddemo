package io.github.mayunfei.rxdownload.download;

import io.github.mayunfei.rxdownload.db.IDownloadDB;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.IOUtils;
import io.github.mayunfei.rxdownload.utils.L;
import io.github.mayunfei.rxdownload.utils.RxUtils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import retrofit2.Response;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.DOWNLOADING;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.ERROR;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.FINISH;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.PAUSE;
import static io.github.mayunfei.rxdownload.utils.RxUtils.createProcessor;
import static io.github.mayunfei.rxdownload.utils.RxUtils.retry;

/**
 * 真正下载在这里
 * Created by yunfei on 17-3-25.
 */

public class DownloadTask {

  private DownloadApi mDownloadApi;
  private IDownloadDB downloadDB;
  private boolean isCancel = false;

  private DownloadBundle downloadBundle;
  private List<ItemTask> itemTasks;
  private DownloadEvent event;
  private AtomicLong completeSize;
  private AtomicLong failSize;
  private TaskObserver taskObserver;
  /**
   * 用于通知下载状态
   */
  private FlowableProcessor<DownloadEvent> processorEvent;

  public DownloadTask(DownloadBundle downloadBundle) {
    this.downloadBundle = downloadBundle;
  }

  public void init(DownloadApi downloadApi) {
    this.mDownloadApi = downloadApi;
    itemTasks = new ArrayList<>();
    taskObserver = new TaskObserver();
  }

  /**
   * 下载开始
   */
  public void startDownload(final Semaphore semaphore) throws InterruptedException {
    //控制信号量
    List<DownloadBean> downloadList = getUnFinished(downloadBundle.getDownloadList());
    if (downloadList.size() == 0) {
      //已经下载完毕 直接结束
    }
    //TODO 判断是否正确
    int unDownloadSize = downloadList.size();

    completeSize = new AtomicLong(downloadBundle.getCompletedSize());
    failSize = new AtomicLong(0);

    event = new DownloadEvent();
    event.setCompletedSize(downloadBundle.getCompletedSize());
    event.setTotalSize(downloadBundle.getTotalSize());
    event.setStatus(DOWNLOADING);
    processorEvent.onNext(event);
    if (isCancel){
      return;
    }

    for (DownloadBean bean : downloadList) {
      itemTasks.add(new ItemTask(mDownloadApi, downloadDB, bean));
    }

    for (ItemTask task : itemTasks) {
      task.startDownload(semaphore, taskObserver);
    }

    //for (int i = 0; i < downloadList.size() && !isCancel; i++) {
    //  DownloadBean bean = downloadList.get(i);
    //  if (isCancel) {
    //    L.i("isCancel");
    //    break;
    //  }
    //  disposable = download(bean).subscribeOn(Schedulers.io())
    //      //取消下载
    //      .doOnCancel(new Action() {
    //        @Override public void run() throws Exception {
    //          downloadBundle.setStatus(PAUSE);
    //          downloadDB.updateDownloadBundle(downloadBundle);
    //        }
    //      }).doFinally(new Action() {
    //        @Override public void run() throws Exception {
    //          if (failSize.longValue() + completeSize.longValue()
    //              == downloadBundle.getTotalSize()) {
    //            if (completeSize.get() == downloadBundle.getTotalSize()) {
    //
    //              downloadBundle.setStatus(FINISH);
    //              downloadDB.updateDownloadBundle(downloadBundle);
    //
    //              event.setStatus(FINISH);
    //              processorEvent.onNext(event);
    //            } else {
    //
    //              downloadBundle.setStatus(ERROR);
    //              downloadDB.updateDownloadBundle(downloadBundle);
    //
    //              event.setStatus(ERROR);
    //              processorEvent.onNext(event);
    //            }
    //            semaphore.release();
    //          }
    //        }
    //      }).subscribe(new Consumer<DownloadEvent>() {
    //        @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
    //
    //        }
    //      }, new Consumer<Throwable>() {
    //        @Override public void accept(@NonNull Throwable throwable) throws Exception {
    //          failSize.incrementAndGet();
    //          L.i("error " + failSize + "      " + throwable.toString());
    //        }
    //      }, new Action() {
    //        @Override public void run() throws Exception {
    //          completeSize.incrementAndGet();
    //          long completed = completeSize.longValue();
    //
    //          downloadBundle.setStatus(DOWNLOADING);
    //          downloadBundle.setCompletedSize(completed);
    //          downloadDB.updateDownloadBundle(downloadBundle);
    //
    //          event.setCompletedSize(completed);
    //          event.setStatus(DOWNLOADING);
    //          processorEvent.onNext(event);
    //          L.i("finished " + completeSize);
    //        }
    //      });
    //  if (isCancel) {
    //    break;
    //  }
    //}
  }

  private List<DownloadBean> getUnFinished(List<DownloadBean> downloadList) {
    List<DownloadBean> unDownload = new ArrayList<>();
    for (DownloadBean bean : downloadList) {
      if (bean.getTotalSize() != bean.getCompletedSize()) {
        unDownload.add(bean);
      }
    }
    return unDownload;
  }

  public void init(Map<String, DownloadTask> taskMap,
      Map<String, FlowableProcessor<DownloadEvent>> processorMap, IDownloadDB downloadDB) {
    DownloadTask task = taskMap.get(downloadBundle.getKey());
    if (task == null) {
      taskMap.put(downloadBundle.getKey(), this);
    } else {
      if (task.isCancel) {
        DownloadBundle oldBundle = task.downloadBundle;
        this.downloadBundle.init(oldBundle);
        taskMap.put(this.downloadBundle.getKey(), this);
      } else {
        //已经存在
        throw new IllegalArgumentException("已经存在了  "+downloadBundle.getKey());
      }
    }
    processorEvent = createProcessor(downloadBundle.getKey(), processorMap);
    this.downloadDB = downloadDB;
  }

  //public Observable<DownloadEvent> startDownload(DownloadBundle downloadBundle) {
  //
  //  final DownloadBean downloadBean = downloadBundle.getDownloadList().get(0);
  //  //download();
  //  Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {
  //    @Override public void accept(@NonNull Subscription subscription) throws Exception {
  //      //插入数据库
  //
  //    }
  //  }).flatMap(new Function<Integer, Publisher<DownloadEvent>>() {
  //    @Override public Publisher<DownloadEvent> apply(@NonNull Integer integer) throws Exception {
  //      return download(downloadBean);
  //    }
  //  }).doOnNext(new Consumer<DownloadEvent>() {
  //    @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
  //      //更新数据库
  //    }
  //  }).doOnError(new Consumer<Throwable>() {
  //    @Override public void accept(@NonNull Throwable throwable) throws Exception {
  //      //更新数据库 失败
  //    }
  //  }).doOnCancel(new Action() {
  //    @Override public void run() throws Exception {
  //      //更新数据库 暂停
  //    }
  //  }).toObservable();
  //
  //  List<DownloadBean> beanList = downloadBundle.getDownloadList();
  //
  //  final PublishProcessor<DownloadEvent> publisher = PublishProcessor.create();
  //
  //  Flowable.fromIterable(beanList).filter(new Predicate<DownloadBean>() {
  //    @Override public boolean test(@NonNull DownloadBean downloadBean) throws Exception {
  //      return downloadBean.getCompletedSize() != downloadBean.getTotalSize();
  //    }
  //  }).doOnSubscribe(new Consumer<Subscription>() {
  //    @Override public void accept(@NonNull Subscription subscription) throws Exception {
  //      publisher.onSubscribe(subscription);
  //    }
  //  }).flatMap(new Function<DownloadBean, Flowable<DownloadEvent>>() {
  //    @Override public Flowable<DownloadEvent> apply(@NonNull DownloadBean downloadBean)
  //        throws Exception {
  //      return download(downloadBean);
  //    }
  //  }).subscribe(new Consumer<DownloadEvent>() {
  //    @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
  //      //子任务更新
  //    }
  //  }, new Consumer<Throwable>() {
  //    @Override public void accept(@NonNull Throwable throwable) throws Exception {
  //      //失败一个
  //    }
  //  }, new Action() {
  //    @Override public void run() throws Exception {
  //      //增加一个
  //    }
  //  });
  //  return publisher.toObservable();
  //}

  public void insertOrUpdate() {
    if (downloadDB.existsDownloadBundle(downloadBundle.getKey())) {

    } else {
      downloadDB.insertDownloadBundle(downloadBundle);
    }
  }

  public void pause() {
    isCancel = true;
    //从数据库查询出它的 下载进度再设置 暂停
    for (ItemTask task : itemTasks) {
      task.pause();
    }
    if (processorEvent != null) {
      DownloadEvent downloadEvent = downloadDB.selectBundleStatus(downloadBundle.getKey());
      downloadEvent.setStatus(PAUSE);
      processorEvent.onNext(downloadEvent);
    }
  }

  //
  //Flowable.fromIterable(downloadList)
  //    .flatMap(new Function<DownloadBean, Flowable<DownloadEvent>>() {
  //      @Override public Flowable<DownloadEvent> apply(@NonNull DownloadBean downloadBean)
  //          throws Exception {
  //        return download(downloadBean);
  //      }
  //    })
  //    .subscribeOn(Schedulers.io())
  //    .doFinally(new Action() {
  //      @Override public void run() throws Exception {
  //        if (completeSize.get() == downloadBundle.getTotalSize()) {
  //          event.setStatus(FINISH);
  //          processorEvent.onNext(event);
  //        } else {
  //          event.setStatus(ERROR);
  //          processorEvent.onNext(event);
  //        }
  //        semaphore.release();
  //      }
  //    })
  //    .subscribe(new Consumer<DownloadEvent>() {
  //      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
  //      }
  //    }, new Consumer<Throwable>() {
  //      @Override public void accept(@NonNull Throwable throwable) throws Exception {
  //        L.e("error " + throwable);
  //        failSize.incrementAndGet();
  //      }
  //    }, new Action() {
  //      @Override public void run() throws Exception {
  //        L.i("finished");
  //        long completed = completeSize.incrementAndGet();
  //        event.setCompletedSize(completed);
  //        event.setStatus(DOWNLOADING);
  //        processorEvent.onNext(event);
  //      }
  //    });

  public class TaskObserver implements Observer<DownloadEvent> {

    @Override public void onSubscribe(Disposable d) {
      //event.setStatus(DOWNLOADING);
      //processorEvent.onNext(event);
    }

    @Override public void onNext(DownloadEvent downloadEvent) {

    }

    @Override public void onError(Throwable e) {
      //错误++
      L.i("onError-----------" + e.toString());
      failSize.incrementAndGet();
      checkFinished();
    }

    @Override public void onComplete() {
      //正确++
      L.i("onComplete--------------------------");

      completeSize.incrementAndGet();

      L.i("onComplete----------" + completeSize.longValue() + "----------------");

      if (!checkFinished()) {
        downloadBundle.setCompletedSize(completeSize.longValue());
        downloadBundle.setStatus(DOWNLOADING);
        event.setCompletedSize(completeSize.longValue());
        downloadDB.updateDownloadBundle(downloadBundle);
        event.setStatus(DOWNLOADING);
        processorEvent.onNext(event);
      }
    }

    private boolean checkFinished() {
      if (failSize.longValue() + completeSize.longValue() == downloadBundle.getTotalSize()) {
        if (completeSize.get() == downloadBundle.getTotalSize()) {

          downloadBundle.setCompletedSize(completeSize.longValue());
          downloadBundle.setStatus(FINISH);
          downloadDB.updateDownloadBundle(downloadBundle);

          event.setCompletedSize(completeSize.longValue());
          event.setStatus(FINISH);
          processorEvent.onNext(event);
        } else {

          downloadBundle.setStatus(ERROR);
          downloadBundle.setCompletedSize(completeSize.longValue());
          downloadDB.updateDownloadBundle(downloadBundle);

          event.setStatus(ERROR);
          event.setCompletedSize(completeSize.longValue());

          processorEvent.onNext(event);
        }
        return true;
      }
      return false;
    }
  }
}
