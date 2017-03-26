package io.github.mayunfei.downloaddemo;

import android.Manifest;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.util.ArrayList;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private Retrofit retrofit;
  private RxDownloadManager rxDownloadManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn).setOnClickListener(this);
    retrofit = new Retrofit.Builder().baseUrl("http://www.exam.com/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();
    rxDownloadManager = new RxDownloadManager();
    rxDownloadManager.init(this, retrofit);
    ActivityCompat.requestPermissions(this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
  }

  @Override public void onClick(View v) {

    DownloadBundle downloadBundle = new DownloadBundle();
    ArrayList<DownloadBean> beanlist = new ArrayList<>();

    DownloadBean bean0 = new DownloadBean();
    //DownloadBean bean1 = new DownloadBean();
    //DownloadBean bean2 = new DownloadBean();
    bean0.setUrl("http://s1.music.126.net/download/android/CloudMusic_2.8.1_official_4.apk");
    bean0.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    bean0.setFileName("yunfei.apk");
    beanlist.add(bean0);
    downloadBundle.setDownloadList(beanlist);
    downloadBundle.setTotalSize(beanlist.size());
    downloadBundle.setKey("yunfei");

    rxDownloadManager.addDownladTask(new DownloadTask(downloadBundle))
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {

          }
        });
  }
}
