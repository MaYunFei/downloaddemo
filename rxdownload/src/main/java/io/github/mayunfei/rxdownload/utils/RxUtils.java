package io.github.mayunfei.rxdownload.utils;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiPredicate;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;

/**
 * Created by yunfei on 17-3-26.
 */

public class RxUtils {
  private static final int RETRY_COUNT = 3;

  private RxUtils() {

  }

  /**
   * 下载重试
   */
  public static <T> FlowableTransformer<T, T> retry(final String message) {
    return new FlowableTransformer<T, T>() {
      @Override public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.retry(new BiPredicate<Integer, Throwable>() {
          @Override public boolean test(@NonNull Integer integer, @NonNull Throwable throwable) throws Exception {
            return retry(message, integer, throwable);
          }
        });
      }
    };
  }

  /**
   * 重试规则
   */
  private static boolean retry(String msg, Integer count, Throwable throwable) {
    if (throwable instanceof ProtocolException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else if (throwable instanceof UnknownHostException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else if (throwable instanceof HttpException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else if (throwable instanceof SocketTimeoutException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else if (throwable instanceof ConnectException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else if (throwable instanceof SocketException) {
      if (count < RETRY_COUNT + 1) {
        return true;
      }
      return false;
    } else {
      return false;
    }
  }
}
