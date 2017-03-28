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
    findViewById(R.id.btn).setOnClickListener(this);
    findViewById(R.id.btn_pause).setOnClickListener(this);
    findViewById(R.id.btn_list).setOnClickListener(this);

    ActivityCompat.requestPermissions(this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
    disposable = RxDownloadManager.getInstance()
        .getDownloadEvent("yunfei")
        .subscribe(new Consumer<DownloadEvent>() {
          @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
            Log.i("******event******yunfei", downloadEvent.toString());
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.e(TAG, throwable.toString());
          }
        });
    //rxDownloadManager.getDownloadEvent(
    //    "http://img.wdjimg.com/mms/icon/v1/8/10/1b26d9f0a258255b0431c03a21c0d108_512_512.png")
    //    .subscribe(new Consumer<DownloadEvent>() {
    //      @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
    //        Log.i("****event*youkuweb.apk", downloadEvent.toString());
    //      }
    //    }, new Consumer<Throwable>() {
    //      @Override public void accept(@NonNull Throwable throwable) throws Exception {
    //        L.e(TAG, throwable.toString());
    //      }
    //    });
    //
    RxDownloadManager.getInstance()
        .getAllDownloadBundle()
        .subscribe(new Consumer<List<DownloadBundle>>() {
          @Override public void accept(@NonNull List<DownloadBundle> downloadBundles)
              throws Exception {
            Observable.fromIterable(downloadBundles).subscribe(new Consumer<DownloadBundle>() {
              @Override public void accept(@NonNull DownloadBundle downloadBundle)
                  throws Exception {
                L.i(TAG, downloadBundle.toString());
              }
            });
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {

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
      case R.id.btn_list:
        startActivity(new Intent(this, DownloadListActivity.class));
        break;
    }
  }

  private void pause() {
    RxDownloadManager.getInstance().pause("yunfei").subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {

      }
    });
    RxDownloadManager.getInstance()
        .pause(
            "http://img.wdjimg.com/mms/icon/v1/8/10/1b26d9f0a258255b0431c03a21c0d108_512_512.png")
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {

          }
        });
  }

  private void download() {
    //DownloadBundle downloadBundle = new DownloadBundle();
    //ArrayList<DownloadBean> beanlist = new ArrayList<>();
    //
    //DownloadBean bean0 = new DownloadBean();
    //DownloadBean bean1 = new DownloadBean();
    //DownloadBean bean2 = new DownloadBean();
    //DownloadBean bean3 = new DownloadBean();
    //bean0.setFileName("000.png");
    //bean0.setUrl(
    //    "http://img.wdjimg.com/mms/icon/v1/d/f1/1c8ebc9ca51390cf67d1c3c3d3298f1d_512_512.png");
    //bean0.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
    //    + File.separator
    //    + "rxdownload");
    //File file = new File(bean0.getPath());
    //file.mkdirs();
    //bean1.setFileName("001.png");
    //bean1.setUrl(
    //    "http://img.wdjimg.com/mms/icon/v1/3/2d/dc14dd1e40b8e561eae91584432262d3_512_512.png");
    //bean1.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
    //    + File.separator
    //    + "rxdownload");
    //bean2.setFileName("002.png");
    //bean2.setUrl(
    //    "http://img.wdjimg.com/mms/icon/v1/8/10/1b26d9f0a258255b0431c03a21c0d108_512_512.pn");
    //bean2.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
    //    + File.separator
    //    + "rxdownload");
    //
    //bean3.setFileName("official_4.apk");
    //bean3.setUrl("http://s1.music.126.net/download/android/CloudMusic_2.8.1_official_4.apk");
    //bean3.setPath(Environment.getExternalStorageDirectory().getAbsolutePath()
    //    + File.separator
    //    + "rxdownload");
    //beanlist.add(bean0);
    //beanlist.add(bean1);
    //beanlist.add(bean2);
    //beanlist.add(bean3);
    //downloadBundle.setDownloadList(beanlist);
    //downloadBundle.setTotalSize(beanlist.size());
    //downloadBundle.setKey("yunfei");
    //RxDownloadManager.getInstance().addDownloadTask(
    //    "http://img.wdjimg.com/mms/icon/v1/8/10/1b26d9f0a258255b0431c03a21c0d108_512_512.png")
    //    .subscribe(new Consumer<Object>() {
    //      @Override public void accept(@NonNull Object o) throws Exception {
    //
    //      }
    //    }, new Consumer<Throwable>() {
    //      @Override public void accept(@NonNull Throwable throwable) throws Exception {
    //        L.e(throwable);
    //      }
    //    });
    //
    //RxDownloadManager.getInstance().addDownloadTask(new DownloadTask(downloadBundle))
    //    .subscribe(new Consumer<Object>() {
    //      @Override public void accept(@NonNull Object o) throws Exception {
    //
    //      }
    //    }, new Consumer<Throwable>() {
    //      @Override public void accept(@NonNull Throwable throwable) throws Exception {
    //        L.e(throwable);
    //      }
    //    });

    RxDownloadManager.getInstance()
        .addDownloadTask("m3u8",
            "https://md.dongaocloud.com/2b4f/2b52/5b3/81e/61e08244fcd53892b90031ee873de2b2/video.m3u8",
            "http://www.jianshu.com/p/94c433057440")
        .subscribe(new Consumer<Object>() {
          @Override public void accept(@NonNull Object o) throws Exception {

          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            L.i(TAG, throwable.toString());
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
