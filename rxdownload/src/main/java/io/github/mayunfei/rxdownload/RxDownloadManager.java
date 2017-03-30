package io.github.mayunfei.rxdownload;

import android.content.Context;
import android.os.Environment;
import io.github.mayunfei.rxdownload.download.DownloadApi;
import io.github.mayunfei.rxdownload.download.DownloadService;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.download.ServiceHelper;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.FileUtils;
import io.github.mayunfei.rxdownload.utils.ParserUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Retrofit;

/**
 * Created by yunfei on 17-3-25.
 */

public class RxDownloadManager {
  private ServiceHelper serviceHelper;
  private DownloadApi downloadApi;
  private String defPath = FileUtils.getDefaultFilePath();

  private static RxDownloadManager instance = new RxDownloadManager();

  private RxDownloadManager() {
  }

  public static RxDownloadManager getInstance() {
    return instance;
  }

  public void init(Context context, Retrofit retrofit) {
    serviceHelper = new ServiceHelper(context);
    downloadApi = retrofit.create(DownloadApi.class);
  }

  public Observable<?> addDownloadTask(final String key, String m3u8, String html) {

    //return Observable.merge(ParserUtils.m3u8Parser(downloadApi, m3u8, defPath),
    //    ParserUtils.htmlParser(downloadApi, html, defPath))
    return ParserUtils.m3u8Parser(downloadApi, m3u8, defPath + File.separator + key)
        .toList()
        .toObservable()
        .flatMap(new Function<List<DownloadBean>, ObservableSource<?>>() {
          @Override public ObservableSource<?> apply(@NonNull List<DownloadBean> downloadBeen)
              throws Exception {
            DownloadBundle downloadBundle = new DownloadBundle();
            downloadBundle.setKey(key);
            downloadBundle.setDownloadList(downloadBeen);
            downloadBundle.setTotalSize(downloadBeen.size());
            return addDownloadTask(new DownloadTask(downloadBundle));
          }
        })
        .subscribeOn(Schedulers.io());
  }

  public Observable<?> addDownloadTask(String url) {
    DownloadBean bean = DownloadBean.newBuilder()
        .url(url)
        .fileName(FileUtils.getFileNameFromUrl(url))
        .path(defPath)
        .build();

    List<DownloadBean> downloadBeanList = new ArrayList<>();
    downloadBeanList.add(bean);
    DownloadBundle downloadBundle = new DownloadBundle();
    downloadBundle.setKey(url);
    downloadBundle.setDownloadList(downloadBeanList);
    downloadBundle.setTotalSize(downloadBeanList.size());
    return addDownloadTask(new DownloadTask(downloadBundle));
  }

  public Observable<?> addDownloadTask(final DownloadTask downloadTask) {
    downloadTask.init(downloadApi);
    return serviceHelper.addTask(downloadTask).observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<DownloadEvent> getDownloadEvent(String key) {
    return serviceHelper.getDownloadEvent(key).observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<?> pause(String key) {
    return serviceHelper.pause(key);
  }

  public Observable<List<DownloadBundle>> getAllDownloadBundle() {
    return serviceHelper.getAllDownloadBundle();
  }
}
