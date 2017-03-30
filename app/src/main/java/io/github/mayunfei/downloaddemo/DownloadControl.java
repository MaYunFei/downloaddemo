package io.github.mayunfei.downloaddemo;

import android.widget.Button;
import io.github.mayunfei.rxdownload.entity.DownloadStatus;

/**
 * Created by yunfei on 17-3-30.
 */

public class DownloadControl {
  private Button mAction;
  private DownloadState mState;

  public DownloadControl(Button mAction) {
    this.mAction = mAction;
    this.mState = new Normal();
  }

  public void handleClick(Callback callback) {
    mState.handleClick(callback);
  }


  public void setStatus(int status) {
    switch (status) {
      case DownloadStatus.DOWNLOADING:
        mState = new Normal();
        mState.setText(mAction);
        break;
      case DownloadStatus.FINISH:
        mState = new Finished();
        mState.setText(mAction);
        break;
      case DownloadStatus.QUEUE:
        mState = new Waiting();
        mState.setText(mAction);
        break;
    }
  }

  public interface Callback {
    void startDownload();

    void pauseDownload();

    void delete();
  }

  static abstract class DownloadState {

    abstract void setText(Button button);

    abstract void handleClick(Callback callback);
  }

  public static class Normal extends DownloadState {

    @Override void setText(Button button) {
      button.setText("下载");
    }

    @Override void handleClick(Callback callback) {
      callback.pauseDownload();
    }
  }

  public static class Waiting extends DownloadState {
    @Override void setText(Button button) {
      button.setText("等待中");
    }

    @Override void handleClick(Callback callback) {
      callback.pauseDownload();
    }
  }

  public static class Finished extends DownloadState {

    @Override void setText(Button button) {
      button.setText("删除");
    }

    @Override void handleClick(Callback callback) {
      callback.delete();
    }
  }

  public static class Pause extends DownloadState {

    @Override void setText(Button button) {
      button.setText("暂停");
    }

    @Override void handleClick(Callback callback) {
      callback.startDownload();
    }
  }
}
