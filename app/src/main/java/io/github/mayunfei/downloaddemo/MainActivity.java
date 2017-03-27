package io.github.mayunfei.downloaddemo;

import android.Manifest;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
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
    findViewById(R.id.btn_pause).setOnClickListener(this);
    retrofit = new Retrofit.Builder().baseUrl("http://www.exam.com/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();
    rxDownloadManager = new RxDownloadManager();
    rxDownloadManager.init(this, retrofit);
    ActivityCompat.requestPermissions(this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn:
        download();
        break;
      case R.id.btn_pause:
        pause();
        break;
    }
  }

  private void pause() {
    rxDownloadManager.pause("yunfei").subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {

      }
    });
  }

  private void download() {
    DownloadBundle downloadBundle = new DownloadBundle();
    ArrayList<DownloadBean> beanlist = new ArrayList<>();

    DownloadBean bean0 = new DownloadBean();
    DownloadBean bean1 = new DownloadBean();
    DownloadBean bean2 = new DownloadBean();
    DownloadBean bean3 = new DownloadBean();
    bean0.setFileName("000.ts");
    bean0.setUrl(
        "https://mv.dongaocloud.com/2b4f/2b51/d42/278/d704d5c7c226a371f8b34926f14330f0/d704d5c7c226a371f8b34926f14330f0-000.ts");
    bean0.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    File file = new File(bean0.getPath());
    file.mkdirs();
    bean1.setFileName("001.ts");
    bean1.setUrl(
        "https://mv.dongaocloud.com/2b4f/2b51/d42/278/d704d5c7c226a371f8b34926f14330f0/d704d5c7c226a371f8b34926f14330f0-00.ts");
    bean1.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    bean2.setFileName("002.ts");
    bean2.setUrl(
        "https://mv.dongaocloud.com/2b4f/2b51/d42/278/d704d5c7c226a371f8b34926f14330f0/d704d5c7c226a371f8b34926f14330f0-002.ts");
    bean2.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");

    bean3.setFileName("official_4.apk");
    bean3.setUrl("http://s1.music.126.net/download/android/CloudMusic_2.8.1_official_4.apk");
    bean3.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    beanlist.add(bean0);
    beanlist.add(bean1);
    beanlist.add(bean2);
    beanlist.add(bean3);
    downloadBundle.setDownloadList(beanlist);
    downloadBundle.setTotalSize(beanlist.size());
    downloadBundle.setKey("yunfei");

    rxDownloadManager.addDownloadTask(new DownloadTask(downloadBundle))
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {

          }
        });

    rxDownloadManager.getDownloadEvent("yunfei").subscribe(new Consumer<DownloadEvent>() {
      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
        Log.i("******event******", downloadEvent.toString());
      }
    });
  }
}
