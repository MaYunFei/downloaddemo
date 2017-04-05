package io.github.mayunfei.downloaddemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * Created by mayunfei on 17-4-5.
 */

public class SpUtil {
  private static final String DOWNLOAD_SP = "download_path";
  private static final String DOWNLOAD_PATH = "download_path";
  private static final Object OBJECT = new Object();

  public static Observable<Object> putPath(final Context context, final String path) {
    return Observable.just(1).map(new Function<Integer, Object>() {
      @Override public Object apply(@NonNull Integer integer) throws Exception {
        SharedPreferences sharedPreferences =
            context.getSharedPreferences(DOWNLOAD_SP, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(DOWNLOAD_PATH, path).apply();
        return OBJECT;
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  public static Observable<String> getPath(final Context context) {

    final Context appContext = context.getApplicationContext();

    return Observable.just(1).map(new Function<Integer, String>() {
      @Override public String apply(@NonNull Integer integer) throws Exception {
        return appContext.getSharedPreferences(DOWNLOAD_SP, Context.MODE_PRIVATE)
            .getString(DOWNLOAD_PATH, "");
      }
    }).flatMap(new Function<String, ObservableSource<String>>() {
      @Override public ObservableSource<String> apply(@NonNull String s) throws Exception {
        if (TextUtils.isEmpty(s)) {
          return StorageUtils.getObservableStorageBean(appContext)
              .map(new Function<List<StorageBean>, String>() {
                @Override public String apply(@NonNull List<StorageBean> storageBeanList)
                    throws Exception {

                  for (int i = 0; i < storageBeanList.size(); i++) {
                    StorageBean storageBean = storageBeanList.get(i);
                    if (!storageBean.getRemovable()) {
                      return storageBean.getPath();
                    }
                  }
                  return null;
                }
              });
        } else {
          return Observable.just(s);
        }
      }
    })

        ;
  }
}
