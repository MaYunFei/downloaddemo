package io.github.mayunfei.rxdownload;

import android.content.Context;
import io.github.mayunfei.rxdownload.download.DownloadApi;
import io.github.mayunfei.rxdownload.download.DownloadService;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.download.ServiceHelper;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.reactivex.Observable;
import java.util.concurrent.Semaphore;
import retrofit2.Retrofit;

/**
 * Created by yunfei on 17-3-25.
 */

public class RxDownloadManager {
  private ServiceHelper serviceHelper;
  private DownloadApi downloadApi;
  //Service
  private static final Object object = new Object();
  private volatile static boolean bound = false;
  private int maxDownloadNumber = 2;
  private Context context;
  //信号量  用于绑定service
  private Semaphore semaphore;

  private DownloadService downloadService;



  public void init(Context context, Retrofit retrofit) {
    serviceHelper = new ServiceHelper(context);
    downloadApi = retrofit.create(DownloadApi.class);
    this.context = context.getApplicationContext();
  }

  public Observable<?> addDownladTask(final DownloadTask downloadTask) {
    return serviceHelper.createGeneralObservable(new ServiceHelper.GeneralObservableCallback() {
      @Override public void call(DownloadService downloadService) throws Exception {
        downloadTask.init(downloadApi);
        downloadService.addTask(downloadTask);
      }
    });
  }

  //public Observable<DownloadEvent> getDownloadEvent(String key){
  //  return serviceHelper.createGeneralObservable(new ServiceHelper.GeneralObservableCallback() {
  //    @Override public void call(DownloadService downloadService) throws Exception {
  //
  //    }
  //  })
  //}
}
