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
    rxDownloadManager = RxDownloadManager.getInstance();
    rxDownloadManager.init(this, retrofit);
    ActivityCompat.requestPermissions(this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
    rxDownloadManager.getDownloadEvent("yunfei").subscribe(new Consumer<DownloadEvent>() {
      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
        Log.i("******event******", downloadEvent.toString());
      }
    });
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
    bean0.setFileName("000.png");
    bean0.setUrl(
        "http://img.wdjimg.com/mms/icon/v1/d/f1/1c8ebc9ca51390cf67d1c3c3d3298f1d_512_512.png");
    bean0.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    File file = new File(bean0.getPath());
    file.mkdirs();
    bean1.setFileName("001.png");
    bean1.setUrl(
        "http://img.wdjimg.com/mms/icon/v1/3/2d/dc14dd1e40b8e561eae91584432262d3_512_512.png");
    bean1.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator
        + "rxdownload");
    bean2.setFileName("002.png");
    bean2.setUrl(
        "http://img.wdjimg.com/mms/icon/v1/8/10/1b26d9f0a258255b0431c03a21c0d108_512_512.pn");
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
  }
}
