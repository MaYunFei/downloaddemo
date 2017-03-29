package io.github.mayunfei.rxdownload.download;

import io.github.mayunfei.rxdownload.db.IDownloadDB;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.entity.DownloadStatus;
import io.github.mayunfei.rxdownload.utils.L;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.DOWNLOADING;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.ERROR;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.FINISH;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.PAUSE;
import static io.github.mayunfei.rxdownload.utils.RxUtils.createProcessor;

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
    event = new DownloadEvent();
    event.setCompletedSize(downloadBundle.getCompletedSize());
    event.setTotalSize(downloadBundle.getTotalSize());
    List<DownloadBean> downloadList = getUnFinished(downloadBundle.getDownloadList());
    if (downloadList.size() == 0) {
      //已经下载完毕 直接结束
      event.setStatus(FINISH);
      processorEvent.onNext(event);
      return;
    }
    //TODO 判断是否正确
    int unDownloadSize = downloadList.size();

    completeSize = new AtomicLong(downloadBundle.getCompletedSize());
    failSize = new AtomicLong(0);

    event.setStatus(DOWNLOADING);
    processorEvent.onNext(event);
    if (isCancel) {
      return;
    }

    for (DownloadBean bean : downloadList) {
      itemTasks.add(new ItemTask(mDownloadApi, downloadDB, bean));
    }

    for (ItemTask task : itemTasks) {
      task.startDownload(semaphore, taskObserver);
    }
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
        throw new IllegalArgumentException("已经存在了  " + downloadBundle.getKey());
      }
    }
    processorEvent = createProcessor(downloadBundle.getKey(), processorMap);
    this.downloadDB = downloadDB;
  }

  public void insertOrUpdate() {
    if (downloadDB.existsDownloadBundle(downloadBundle.getKey())) {
      downloadBundle.init(downloadDB.getDownloadBundle(downloadBundle.getKey()));
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

  public boolean isFinished() {
    return downloadBundle.getStatus() == DownloadStatus.FINISH;
  }

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
      //正确+
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
        isCancel = true;
        return true;
      }
      return false;
    }
  }

  public String getKey() {
    return downloadBundle.getKey();
  }

  public boolean isCancel() {
    return isCancel;
  }
}
