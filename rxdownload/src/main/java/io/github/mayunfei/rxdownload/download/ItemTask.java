package io.github.mayunfei.rxdownload.download;

import io.github.mayunfei.rxdownload.db.IDownloadDB;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.IOUtils;
import io.github.mayunfei.rxdownload.utils.L;
import io.github.mayunfei.rxdownload.utils.RxUtils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import retrofit2.Response;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.DOWNLOADING;

/**
 * Created by mayunfei on 17-3-27.
 */

public class ItemTask {

  private static final long DOWNLOAD_CHUNK_SIZE = 2048;
  private static final String TAG = "11111111111";
  private DownloadApi mDownloadApi;
  private IDownloadDB downloadDB;
  //用于取消
  private Disposable disposable;
  private DownloadBean downloadBean;

  boolean isCancel = false;

  public void pause() {
    isCancel = true;
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  public ItemTask(DownloadApi mDownloadApi, IDownloadDB downloadDB, DownloadBean downloadBean) {
    this.mDownloadApi = mDownloadApi;
    this.downloadDB = downloadDB;
    this.downloadBean = downloadBean;
  }

  public void startDownload(final Semaphore semaphore, final DownloadTask.TaskObserver taskObserver)
      throws InterruptedException {
    if (isCancel) {
      return;
    }
    semaphore.acquire();
    if (isCancel) {
      return;
    }
    disposable = download(downloadBean).toObservable()
        .subscribeOn(Schedulers.io())
        .doOnSubscribe(new Consumer<Disposable>() {
          @Override public void accept(@NonNull Disposable disposable) throws Exception {
            taskObserver.onSubscribe(disposable);
            L.i(TAG, "start " + downloadBean.getUrl());
          }
        })
        .doFinally(new Action() {
          @Override public void run() throws Exception {
            semaphore.release();
            L.i(TAG, "finished " + downloadBean.getUrl());
          }
        })
        .subscribe(new Consumer<DownloadEvent>() {
          @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {

          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.i(TAG, "error " + downloadBean.getUrl());
            taskObserver.onError(throwable);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            taskObserver.onComplete();
          }
        });
  }

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
        })
        .doOnNext(new Consumer<DownloadEvent>() {
          @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
            downloadBean.setCompletedSize(downloadEvent.getCompletedSize());
            downloadBean.setTotalSize(downloadEvent.getTotalSize());
            downloadDB.updateDownloadBean(downloadBean);
          }
        });
  }

  /**
   * 将请求存储 并重试
   */
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
  private void saveFile(FlowableEmitter<DownloadEvent> emitter, Response<ResponseBody> response,
      String path, String name) {
    BufferedSink sink = null;
    BufferedSource source = null;
    try {
      DownloadEvent downloadEvent = new DownloadEvent();
      ResponseBody body = response.body();
      downloadEvent.setTotalSize(body.contentLength());
      File file = new File(path, name);
      sink = Okio.buffer(Okio.sink(file));
      long totalRead = 0;
      long read = 0;
      downloadEvent.setStatus(DOWNLOADING);
      source = body.source();
      while ((read = (source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE))) != -1
          && !emitter.isCancelled()) {
        totalRead += read;
        downloadEvent.setCompletedSize(totalRead);
        if (!emitter.isCancelled()) {
          emitter.onNext(downloadEvent);
        }
      }

      sink.writeAll(source);
      source.close();
      if (!emitter.isCancelled()) {
        emitter.onComplete();
      }
    } catch (IOException e) {
      //e.printStackTrace();
      if (!emitter.isCancelled()) {
        emitter.onError(e);
      }
    } finally {
      IOUtils.close(sink, source);
    }
  }
}
