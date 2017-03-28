package io.github.mayunfei.downloaddemo;

import android.app.Application;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import retrofit2.Retrofit;

/**
 * Created by mayunfei on 17-3-28.
 */

public class App extends Application {
  @Override public void onCreate() {
    super.onCreate();
    RxDownloadManager.getInstance()
        .init(this, new Retrofit.Builder().baseUrl("http://www.exam.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build());
  }
}
