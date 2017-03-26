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
import static io.github.mayunfei.rxdownload.utils.RxUtils.createProcessor;

/**
 * 真正下载在这里
 * Created by yunfei on 17-3-25.
 */

public class DownloadTask {
  private static final long DOWNLOAD_CHUNK_SIZE = 2048;
  private DownloadApi mDownloadApi;
  private IDownloadDB downloadDB;
  private boolean isCancle = false;
  private Disposable disposable;
  private DownloadBundle downloadBundle;

  public DownloadTask(DownloadBundle downloadBundle) {
    this.downloadBundle = downloadBundle;
  }
  public void init(DownloadApi downloadApi){
    this.mDownloadApi = downloadApi;
  }

  /**
   * 用于通知下载状态
   */
  private FlowableProcessor<DownloadEvent> processorEvent;

  /**
   * 单一任务下载
   */
  private Flowable<DownloadEvent> download(final DownloadBean downloadBean) {
    return mDownloadApi.download(downloadBean.getUrl())
        .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadEvent>>() {
          @Override public Publisher<DownloadEvent> apply(@NonNull Response<ResponseBody> response)
              throws Exception {
            return save(downloadBean, response);
          }
        });
  }

  public Observable<DownloadEvent> startDownload(DownloadBundle downloadBundle) {

    final DownloadBean downloadBean = downloadBundle.getDownloadList().get(0);
    //download();
    Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {
      @Override public void accept(@NonNull Subscription subscription) throws Exception {
        //插入数据库

      }
    }).flatMap(new Function<Integer, Publisher<DownloadEvent>>() {
      @Override public Publisher<DownloadEvent> apply(@NonNull Integer integer) throws Exception {
        return download(downloadBean);
      }
    }).doOnNext(new Consumer<DownloadEvent>() {
      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
        //更新数据库
      }
    }).doOnError(new Consumer<Throwable>() {
      @Override public void accept(@NonNull Throwable throwable) throws Exception {
        //更新数据库 失败
      }
    }).doOnCancel(new Action() {
      @Override public void run() throws Exception {
        //更新数据库 暂停
      }
    }).toObservable();

    List<DownloadBean> beanList = downloadBundle.getDownloadList();

    final PublishProcessor<DownloadEvent> publisher = PublishProcessor.create();

    Flowable.fromIterable(beanList).filter(new Predicate<DownloadBean>() {
      @Override public boolean test(@NonNull DownloadBean downloadBean) throws Exception {
        return downloadBean.getCompletedSize() != downloadBean.getTotalSize();
      }
    }).doOnSubscribe(new Consumer<Subscription>() {
      @Override public void accept(@NonNull Subscription subscription) throws Exception {
        publisher.onSubscribe(subscription);
      }
    }).flatMap(new Function<DownloadBean, Flowable<DownloadEvent>>() {
      @Override public Flowable<DownloadEvent> apply(@NonNull DownloadBean downloadBean)
          throws Exception {
        return download(downloadBean);
      }
    }).subscribe(new Consumer<DownloadEvent>() {
      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
        //子任务更新
      }
    }, new Consumer<Throwable>() {
      @Override public void accept(@NonNull Throwable throwable) throws Exception {
        //失败一个
      }
    }, new Action() {
      @Override public void run() throws Exception {
        //增加一个
      }
    });
    return publisher.toObservable();
  }

  private Publisher<DownloadEvent> save(final DownloadBean bean,
      final Response<ResponseBody> response) {
    return Flowable.create(new FlowableOnSubscribe<DownloadEvent>() {
      @Override public void subscribe(FlowableEmitter<DownloadEvent> e) throws Exception {
        saveFile(e, response, bean.getPath(), bean.getFileName());
      }
    }, BackpressureStrategy.LATEST)
        //重试
        .compose(RxUtils.<DownloadEvent>retry(bean.getUrl()));
  }

  /**
   * 存储下载结果
   */
  private void saveFile(Emitter<DownloadEvent> emitter, Response<ResponseBody> response,
      String path, String name) {
    BufferedSink sink = null;
    BufferedSource source = null;
    try {
      DownloadEvent downloadEvent = new DownloadEvent();
      ResponseBody body = response.body();
      downloadEvent.setCompletedSize(body.contentLength());
      //File fileDir = new File(path);
      //if (!fileDir.exists()) {
      //  fileDir.mkdirs();
      //}
      File file = new File(path, name);
      sink = Okio.buffer(Okio.sink(file));
      long totalRead = 0;
      long read = 0;
      downloadEvent.setStatus(DOWNLOADING);
      source = body.source();
      while ((read = (source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE))) != -1) {
        totalRead += read;
        downloadEvent.setCompletedSize(totalRead);
        emitter.onNext(downloadEvent);
      }

      sink.writeAll(source);
      source.close();
      emitter.onComplete();
    } catch (IOException e) {
      //e.printStackTrace();
      emitter.onError(e);
    } finally {
      IOUtils.close(sink, source);
    }
  }

  /**
   * 下载开始
   */
  public void startDownload(final Semaphore semaphore) throws InterruptedException {
    //控制信号量
    L.i("startDownload");
    semaphore.acquire();
    List<DownloadBean> downloadList = getUnFinished(downloadBundle.getDownloadList());
    if (downloadList.size() == 0) {
      //已经下载完毕 直接结束
    }

    //TODO 判断是否正确
    int unDownloadSize = downloadList.size();
    final AtomicLong completeSize = new AtomicLong(downloadBundle.getCompletedSize());
    final AtomicLong failSize = new AtomicLong(0);

    final DownloadEvent event = new DownloadEvent();
    event.setCompletedSize(downloadBundle.getCompletedSize());
    event.setTotalSize(downloadBundle.getTotalSize());

    Flowable.fromIterable(downloadList)
        .flatMap(new Function<DownloadBean, Flowable<DownloadEvent>>() {
          @Override public Flowable<DownloadEvent> apply(@NonNull DownloadBean downloadBean)
              throws Exception {
            return download(downloadBean);
          }
        }).subscribeOn(Schedulers.io())
        .doFinally(new Action() {
          @Override public void run() throws Exception {
            if (completeSize.get() == downloadBundle.getTotalSize()) {
              event.setStatus(FINISH);
              processorEvent.onNext(event);
            } else {
              event.setStatus(ERROR);
              processorEvent.onNext(event);
            }
            semaphore.release();
          }
        })
        .subscribe(new Consumer<DownloadEvent>() {
          @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
            L.i("downloading ....");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.e("error " + throwable);
            failSize.incrementAndGet();
          }
        }, new Action() {
          @Override public void run() throws Exception {
            L.i("finished");
            long completed = completeSize.incrementAndGet();
            event.setCompletedSize(completed);
            event.setStatus(DOWNLOADING);
            processorEvent.onNext(event);
          }
        });
  }

  private List<DownloadBean> getUnFinished(List<DownloadBean> downloadList) {
    List<DownloadBean> unDownload = new ArrayList<>();
    for (DownloadBean bean : downloadList) {
      unDownload.add(bean);
    }
    return unDownload;
  }

  public void init(Map<String, DownloadTask> taskMap,
      Map<String, FlowableProcessor<DownloadEvent>> processorMap) {
    DownloadTask task = taskMap.get(downloadBundle.getKey());
    if (task == null) {
      taskMap.put(downloadBundle.getKey(), this);
    } else {
      if (task.isCancle) {
        taskMap.put(downloadBundle.getKey(), this);
      } else {
        //已经存在
      }
    }
    processorEvent = createProcessor(downloadBundle.getKey(), processorMap);
  }


}
