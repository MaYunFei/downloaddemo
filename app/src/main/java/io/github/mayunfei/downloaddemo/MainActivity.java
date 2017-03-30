package io.github.mayunfei.downloaddemo;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.github.mayunfei.rxdownload.download.DownloadTask;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.L;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = "MainActivity";
  private Retrofit retrofit;
  private Disposable disposable;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn_task1).setOnClickListener(this);
    findViewById(R.id.btn_task2).setOnClickListener(this);
    findViewById(R.id.btn_task3).setOnClickListener(this);
    findViewById(R.id.btn_gotolist).setOnClickListener(this);

    ActivityCompat.requestPermissions(this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
    L.i("111111111111111111111");
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_task1:
        download();
        break;
      case R.id.btn_task2:
        download2();
        break;
      case R.id.btn_task3:
        break;
      case R.id.btn_gotolist:
        startActivity(new Intent(this, DownloadListActivity.class));
        break;
    }
  }

  private void download2() {
    RxDownloadManager.getInstance()
        .addDownloadTask("m3u80",
            "https://md.dongaocloud.com/2b4f/2b52/5b3/81e/61e08244fcd53892b90031ee873de2b2/video.m3u8",
            "http://www.jianshu.com/p/94c433057440")
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {
            L.i("开始下载");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.e(TAG, throwable.toString());
          }
        });
  }

  private void download() {

    RxDownloadManager.getInstance()
        .addDownloadTask("m3u81",
            "https://md.dongaocloud.com/2b50/2b91/713/82b/b4c2f6282aabdcf936c15871fcc47d3b/video.m3u8",
            "http://www.jianshu.com/p/94c433057440")
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {
            L.i("开始下载");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.e(TAG, throwable.toString());
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
