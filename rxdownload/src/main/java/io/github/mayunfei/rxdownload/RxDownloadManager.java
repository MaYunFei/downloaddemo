package io.github.mayunfei.rxdownload;

import android.content.Context;
import io.github.mayunfei.rxdownload.download.DownloadApi;
import io.github.mayunfei.rxdownload.download.DownloadService;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.download.ServiceHelper;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.reactivex.Observable;
import retrofit2.Retrofit;

/**
 * Created by yunfei on 17-3-25.
 */

public class RxDownloadManager {
  private ServiceHelper serviceHelper;
  private DownloadApi downloadApi;

  public void init(Context context, Retrofit retrofit) {
    serviceHelper = new ServiceHelper(context);
    downloadApi = retrofit.create(DownloadApi.class);
  }

  public Observable<?> addDownladTask(final DownloadTask downloadTask) {
    downloadTask.init(downloadApi);
    return serviceHelper.addTask(downloadTask);
  }

  public Observable<DownloadEvent> getDownloadEvent(String key){
    return serviceHelper.getDownloadEvent(key);
  }
}
