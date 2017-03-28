package io.github.mayunfei.downloaddemo;

import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.reactivex.disposables.Disposable;

/**
 * Created by mayunfei on 17-3-28.
 */

public class DownloadItem {
  private DownloadBundle downloadBundle;
  private Disposable disposable;

  public DownloadBundle getDownloadBundle() {
    return downloadBundle;
  }

  public void setDownloadBundle(DownloadBundle downloadBundle) {
    this.downloadBundle = downloadBundle;
  }

  public Disposable getDisposable() {
    return disposable;
  }

  public void setDisposable(Disposable disposable) {
    this.disposable = disposable;
  }
}
