package io.github.mayunfei.rxdownload.utils;

import io.github.mayunfei.rxdownload.db.IDownloadDB;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiPredicate;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;

/**
 * Created by yunfei on 17-3-26.
 */

public class RxUtils {
  /**
   * 默认重试的次数
   */
  private static final int RETRY_COUNT = 3;

  private RxUtils() {

  }

  /**
   * 下载重试
   */
  public static <T> FlowableTransformer<T, T> retry(final DownloadBean downloadBean,
      final IDownloadDB downloadDB) {
    return new FlowableTransformer<T, T>() {
      @Override public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.retry(new BiPredicate<Integer, Throwable>() {
          @Override public boolean test(@NonNull Integer integer, @NonNull Throwable throwable)
              throws Exception {
            return retry(downloadBean, downloadDB, integer, throwable);
          }
        });
      }
    };
  }

  /**
   * 重试规则
   */
  private static boolean retry(DownloadBean downloadBean, IDownloadDB downloadDB, Integer count,
      Throwable throwable) {
    L.i("重试中.. " + count);
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
      if (downloadBean.getPriority() == DownloadBean.PRIORITY_LOW) {
        downloadBean.setCompletedSize(1);
        downloadBean.setTotalSize(1);
        downloadDB.updateDownloadBean(downloadBean);
      }
      return false;
    } else {
      if (downloadBean.getPriority() == DownloadBean.PRIORITY_LOW) {
        downloadBean.setCompletedSize(1);
        downloadBean.setTotalSize(1);
        downloadDB.updateDownloadBean(downloadBean);
      }
      return false;
    }
  }

  public static FlowableProcessor<DownloadEvent> createProcessor(String key,
      Map<String, FlowableProcessor<DownloadEvent>> processorMap) {

    if (processorMap.get(key) == null) {
      FlowableProcessor<DownloadEvent> processor =
          BehaviorProcessor.<DownloadEvent>create().toSerialized();
      processorMap.put(key, processor);
    }
    return processorMap.get(key);
  }
}
