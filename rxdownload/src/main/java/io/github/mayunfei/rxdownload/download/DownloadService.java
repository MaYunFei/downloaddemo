package io.github.mayunfei.rxdownload.download;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import io.github.mayunfei.rxdownload.db.DownloadDao;
import io.github.mayunfei.rxdownload.db.IDownloadDB;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.L;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.ERROR;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.FINISH;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.PAUSE;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.QUEUE;
import static io.github.mayunfei.rxdownload.utils.RxUtils.createProcessor;

public class DownloadService extends Service {
  private static final String TAG = "2222222222222222";

  public static final String INTENT_KEY = "io.github.mayunfei.rxdownload.max_download_number";
  private DownloadBinder mBinder;

  private BlockingQueue<DownloadTask> downloadQueue;
  private Map<String, DownloadTask> taskMap;
  private Map<String, FlowableProcessor<DownloadEvent>> processorMap;
  private IDownloadDB mDownloadDB;
  //控制线程的信号量
  private Semaphore semaphore;
  private Disposable disposable;

  public DownloadService() {
  }

  @Override public void onCreate() {
    super.onCreate();
    mBinder = new DownloadBinder();
    downloadQueue = new LinkedBlockingQueue<>();
    taskMap = new ConcurrentHashMap<>();
    processorMap = new ConcurrentHashMap<>();
    mDownloadDB = DownloadDao.getSingleton(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    //只会执行一次
    L.i(TAG, "onStartCommand Service");
    //暂停数据
    mDownloadDB.pauseAll();

    if (intent != null) {
      int maxDownloadNumber = intent.getIntExtra(INTENT_KEY, 5);
      semaphore = new Semaphore(5);
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public IBinder onBind(Intent intent) {
    L.i(TAG, "binding Service");
    startDispatch();
    return mBinder;
  }

  private void startDispatch() {
    disposable = Observable.create(new ObservableOnSubscribe<DownloadTask>() {
      @Override public void subscribe(ObservableEmitter<DownloadTask> emitter) throws Exception {
        DownloadTask task;
        while (!emitter.isDisposed()) {
          try {
            task = downloadQueue.take();
          } catch (InterruptedException e) {
            continue;
          }
          emitter.onNext(task);
        }
        emitter.onComplete();
      }
    }).subscribeOn(Schedulers.newThread()).subscribe(new Consumer<DownloadTask>() {
      @Override public void accept(DownloadTask task) throws Exception {
        task.startDownload(semaphore);
      }
    }, new Consumer<Throwable>() {
      @Override public void accept(Throwable throwable) throws Exception {
        L.e(TAG, throwable + "");
      }
    });
  }

  public class DownloadBinder extends Binder {
    public DownloadService getService() {
      return DownloadService.this;
    }
  }

  public void addTask(DownloadTask downloadTask) throws InterruptedException {
    DownloadEvent downloadEvent = mDownloadDB.selectBundleStatus(downloadTask.getKey());
    if (downloadEvent.getStatus() == FINISH) {
      createProcessor(downloadTask.getKey(), processorMap).onNext(downloadEvent);
    } else {
      //初始化
      DownloadTask task = taskMap.get(downloadTask.getKey());
      if (task != null && !task.isCancel()) {
        return;
      }
      downloadTask.init(taskMap, processorMap, mDownloadDB);
      downloadTask.insertOrUpdate();
      downloadEvent.setStatus(QUEUE);
      createProcessor(downloadTask.getKey(), processorMap).onNext(downloadEvent);
      downloadQueue.put(downloadTask);
    }
  }

  public void pause(String key) {
    DownloadTask downloadTask = taskMap.get(key);
    if (downloadTask != null) {
      downloadTask.pause();
    }
  }

  public FlowableProcessor<DownloadEvent> getDownloadEvent(String key) {
    FlowableProcessor<DownloadEvent> processor = createProcessor(key, processorMap);
    DownloadTask task = taskMap.get(key);
    if (task == null) {
      //判断是否有数据库 是否有文件
      DownloadEvent downloadEvent = mDownloadDB.selectBundleStatus(key);
      if (downloadEvent.getTotalSize() == -1) { //数据库没有数据
        downloadEvent.setStatus(ERROR);
        downloadEvent.setCompletedSize(0);
        downloadEvent.setTotalSize(100);
      } else {
        downloadEvent.setStatus(PAUSE);
      }
      processor.onNext(downloadEvent);
    }

    return processor;
  }

  @Override public void onDestroy() {
    L.i(TAG, "onDestroy");
    mDownloadDB.closeDataBase();
    super.onDestroy();
  }

  public Observable<List<DownloadBundle>> getAllDownloadBundle() {
    return Observable.just(1).map(new Function<Integer, List<DownloadBundle>>() {
      @Override public List<DownloadBundle> apply(@NonNull Integer integer) throws Exception {
        return mDownloadDB.getAllDownloadBundle();
      }
    });
  }

  public void pauseAll() {
    for (DownloadTask task : taskMap.values()) {
      task.pause();
    }
  }

  public void startAll() throws InterruptedException {
    for (DownloadTask task : taskMap.values()) {
      if (!task.isFinished()) {
        addTask(task);
      }
    }
  }

  public void startList(DownloadTask... tasks) throws InterruptedException {
    for (DownloadTask task : tasks) {
      addTask(task);
    }
  }
}
