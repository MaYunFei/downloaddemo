package io.github.mayunfei.rxdownload.utils;

import android.util.Log;
import io.github.mayunfei.rxdownload.BuildConfig;

/**
 * Created by mayunfei on 17-3-24.
 */

public class L {
  private static final boolean debug = true;

  private L() {

  }

  private static final String TAG = "yunfei------------";

  public static void i(String msg) {
    i(TAG, msg);
  }

  public static void i(String tag, String msg) {
    if (debug) Log.i(tag, msg);
  }

  public static void e(String msg) {
    e(TAG, msg);
  }

  public static void e(Throwable error) {
    e(TAG, error.toString());
  }

  public static void e(String tag, String msg) {
    if (debug) Log.e(tag, msg);
  }
}
