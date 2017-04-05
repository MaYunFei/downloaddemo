package io.github.mayunfei.downloaddemo;

import android.app.Application;
import android.text.TextUtils;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.List;
import retrofit2.Retrofit;

/**
 * Created by mayunfei on 17-3-28.
 */

public class App extends Application {
  @Override public void onCreate() {
    super.onCreate();
    SpUtil.getPath(this).subscribe(new Consumer<String>() {
      @Override public void accept(@NonNull String s) throws Exception {
        RxDownloadManager.getInstance()
            .init(App.this, new Retrofit.Builder().baseUrl("http://www.exam.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(), s);
      }
    });
  }
}
