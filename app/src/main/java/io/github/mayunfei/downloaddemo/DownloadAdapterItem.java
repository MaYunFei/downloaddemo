package io.github.mayunfei.downloaddemo;

import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.entity.DownloadStatus;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import kale.adapter.item.AdapterItem;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.DOWNLOADING;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.ERROR;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.FINISH;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.PAUSE;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.QUEUE;

/**
 * Created by mayunfei on 17-3-28.
 */

public class DownloadAdapterItem implements AdapterItem<DownloadItem> {
  Button btn_status;
  TextView tv_key;
  ContentLoadingProgressBar progress;
  AppCompatCheckBox checkBox;
  View rootView;
  private Disposable disposable;
  private DownloadControl downloadControl;

  @Override public int getLayoutResId() {
    return R.layout.downloaditem;
  }

  @Override public void bindViews(View view) {
    btn_status = (Button) view.findViewById(R.id.btn_status);
    tv_key = (TextView) view.findViewById(R.id.tv_key);
    progress = (ContentLoadingProgressBar) view.findViewById(R.id.progress);
    checkBox = (AppCompatCheckBox) view.findViewById(R.id.checkbox);
    rootView = view;
    downloadControl = new DownloadControl(btn_status);
  }

  @Override public void setViews() {

  }

  @Override public void handleData(final DownloadItem downloadItem, int i) {
    disposable = RxDownloadManager.getInstance()
        .getDownloadEvent(downloadItem.getDownloadBundle().getKey())
        .subscribe(new Consumer<DownloadEvent>() {
          @Override public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
            String status = "";
            switch (downloadEvent.getStatus()) {
              case DOWNLOADING:
                status = "下载";
                break;
              case PAUSE:
                status = "暂停";
                break;
              case QUEUE:
                status = "等待";
                break;
              case ERROR:
                status = "错误";
                break;
              case FINISH:
                status = "完成";
                break;
            }
            downloadControl.setStatus(downloadEvent.getStatus());

            btn_status.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                downloadControl.handleClick(new DownloadControl.Callback() {
                  @Override public void startDownload() {
                    RxDownloadManager.getInstance()
                        .addDownloadTask(downloadItem.getDownloadBundle())
                        .subscribe(new Consumer<Object>() {
                          @Override public void accept(@NonNull Object o) throws Exception {

                          }
                        });
                  }

                  @Override public void pauseDownload() {
                    RxDownloadManager.getInstance()
                        .pause(downloadItem.getDownloadBundle().getKey())
                        .subscribe(new Consumer<Object>() {
                          @Override public void accept(@NonNull Object o) throws Exception {

                          }
                        });
                  }

                  @Override public void delete() {
                    RxDownloadManager.getInstance()
                        .delete(downloadItem.getDownloadBundle().getKey())
                        .subscribe(new Consumer<Object>() {
                          @Override public void accept(@NonNull Object o) throws Exception {

                          }
                        });
                  }
                });
              }
            });
            progress.setMax(Long.valueOf(downloadEvent.getTotalSize()).intValue());
            progress.setProgress(Long.valueOf(downloadEvent.getCompletedSize()).intValue());
            tv_key.setText(status);
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {

          }
        });

    downloadItem.setDisposable(disposable);
  }
}
