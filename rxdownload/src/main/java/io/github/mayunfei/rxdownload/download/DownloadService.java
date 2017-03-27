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

import static io.github.mayunfei.rxdownload.utils.RxUtils.createProcessor;

public class DownloadService extends Service {
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
    L.i("onStartCommand Service");
    if (intent != null) {
      int maxDownloadNumber = intent.getIntExtra(INTENT_KEY, 5);
      semaphore = new Semaphore(2);
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public IBinder onBind(Intent intent) {
    L.i("binding Service");
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
      }
    });
  }

  public class DownloadBinder extends Binder {
    public DownloadService getService() {
      return DownloadService.this;
    }
  }

  public void addTask(DownloadTask downloadTask) throws InterruptedException {
    //初始化
    downloadTask.init(taskMap, processorMap, mDownloadDB);
    downloadTask.insertOrUpdate();
    downloadQueue.put(downloadTask);
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
    }

    return processor;
  }

  @Override public void onDestroy() {
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
}
