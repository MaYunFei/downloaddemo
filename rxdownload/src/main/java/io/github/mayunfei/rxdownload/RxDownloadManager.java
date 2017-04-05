package io.github.mayunfei.rxdownload;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import io.github.mayunfei.rxdownload.download.DownloadApi;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.download.ServiceHelper;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.FileUtils;
import io.github.mayunfei.rxdownload.utils.ParserUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

  public static final int MAX_DOWNLOAD_COUNT = 5;

  private ServiceHelper serviceHelper;
  private DownloadApi downloadApi;
  private String downloadPath;
  private String rootPath;

  private static String getRootPath() {
    String rootPath = getInstance().rootPath;
    if (TextUtils.isEmpty(rootPath)) {
      throw new IllegalArgumentException("root path not init");
    }
    return rootPath;
  }

  @NonNull private String getDownloadPath(Context context, String rootPath) {
    if (Build.VERSION.SDK_INT >= 19) {
      getFilesDirs(context);
    }
    final String dir = rootPath + "/Android/data/" + context.getPackageName() + "/";
    final File file = new File(dir);
    if (!file.exists()) {
      file.mkdirs();
    }

    return file.getAbsolutePath();
  }

  private static RxDownloadManager instance = new RxDownloadManager();

  private RxDownloadManager() {
  }

  public static RxDownloadManager getInstance() {
    return instance;
  }

  public void init(Context context, Retrofit retrofit, String rootPath) {
    serviceHelper = new ServiceHelper(context);
    downloadApi = retrofit.create(DownloadApi.class);
    this.rootPath = rootPath;
    this.downloadPath = getDownloadPath(context, rootPath);
  }

  /**
   * 记得先在sp 中存储
   */
  public void setRootPath(Context context, String path) {
    downloadPath = getDownloadPath(context, path);
  }

  public Observable<?> addDownloadTask(final String key, String m3u8, String html) {
    return Observable.merge(ParserUtils.m3u8Parser(downloadApi, m3u8, downloadPath),
        ParserUtils.htmlParser(downloadApi, html, downloadPath))
        //return ParserUtils.m3u8Parser(downloadApi, m3u8, downloadPath + File.separator + key)
        .toList().toObservable().flatMap(new Function<List<DownloadBean>, ObservableSource<?>>() {
          @Override public ObservableSource<?> apply(@NonNull List<DownloadBean> downloadBeen)
              throws Exception {
            DownloadBundle downloadBundle = new DownloadBundle();
            downloadBundle.setPath(downloadPath);
            downloadBundle.setKey(key);
            downloadBundle.setDownloadList(downloadBeen);
            downloadBundle.setTotalSize(downloadBeen.size());
            return addDownloadTask(new DownloadTask(downloadBundle));
          }
        }).subscribeOn(Schedulers.io());
  }

  public Observable<?> addDownloadTask(String url) {
    DownloadBean bean = DownloadBean.newBuilder()
        .url(url)
        .fileName(FileUtils.getFileNameFromUrl(url))
        .path(downloadPath)
        .build();

    List<DownloadBean> downloadBeanList = new ArrayList<>();
    downloadBeanList.add(bean);
    DownloadBundle downloadBundle = new DownloadBundle();
    downloadBundle.setKey(url);
    downloadBundle.setDownloadList(downloadBeanList);
    downloadBundle.setTotalSize(downloadBeanList.size());
    return addDownloadTask(new DownloadTask(downloadBundle));
  }

  public Observable<?> addDownloadTask(DownloadBundle downloadBundle) {
    DownloadTask task = new DownloadTask(downloadBundle);
    return addDownloadTask(task);
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

  public Observable<?> delete(String key) {
    return serviceHelper.delete(key);
  }

  public Observable<List<DownloadBundle>> getAllDownloadBundle() {
    return serviceHelper.getAllDownloadBundle();
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) private void getFilesDirs(Context context) {
    try {
      final File[] dirs = context.getExternalFilesDirs(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
