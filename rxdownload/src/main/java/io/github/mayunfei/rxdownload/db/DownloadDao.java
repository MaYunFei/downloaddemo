package io.github.mayunfei.rxdownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by mayunfei on 17-3-23.
 */

public class DownloadDao implements IDownloadDB {

  private volatile static DownloadDao singleton;
  private SQLiteHelper sqLiteHelper;
  private final Object databaseLock = new Object();
  private volatile SQLiteDatabase readableDatabase;
  private volatile SQLiteDatabase writableDatabase;

  private DownloadDao(Context context) {
    sqLiteHelper = new SQLiteHelper(context.getApplicationContext());
  }

  public static DownloadDao getSingleton(Context context) {
    if (singleton == null) {
      synchronized (DownloadDao.class) {
        if (singleton == null) {
          singleton = new DownloadDao(context);
        }
      }
    }
    return singleton;
  }

  @Override
  public void closeDataBase() {
    synchronized (databaseLock) {
      readableDatabase = null;
      writableDatabase = null;
      sqLiteHelper.close();
    }
  }

  private SQLiteDatabase getWritableDatabase() {
    SQLiteDatabase db = writableDatabase;
    if (db == null) {
      synchronized (databaseLock) {
        db = writableDatabase;
        if (db == null) {
          db = writableDatabase = sqLiteHelper.getWritableDatabase();
        }
      }
    }
    return db;
  }

  private SQLiteDatabase getReadableDatabase() {
    SQLiteDatabase db = readableDatabase;
    if (db == null) {
      synchronized (databaseLock) {
        db = readableDatabase;
        if (db == null) {
          db = readableDatabase = sqLiteHelper.getReadableDatabase();
        }
      }
    }
    return db;
  }

  @Override public Observable<Boolean> updateDownloadBean(DownloadBean bean) {
    return null;
  }

  @Override public Observable<Boolean> updateDownloadBundle(DownloadBundle downloadBundle) {
    return null;
  }

  @Override public Observable<Boolean> insertDownloadBundle(final DownloadBundle downloadBundle) {
    return Observable.create(new ObservableOnSubscribe<Boolean>() {
      @Override public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        try {
          db.beginTransaction();
          long insert =
              db.insert(DownloadBundle.TABLE_NAME, null, DownloadBundle.insert(downloadBundle));
          if (insert <= 0) {
            emitter.onNext(false);
          }
          int bundleId = getLastInsertRowId(db, DownloadBundle.TABLE_NAME);
          if (bundleId == -1) {
            emitter.onNext(false);
          }

          for (DownloadBean bean : downloadBundle.getDownloadList()) {
            bean.setBundleId(bundleId);
            db.insert(DownloadBean.TABLE_NAME, null, DownloadBean.insert(bean));
            int beanId = getLastInsertRowId(db, DownloadBean.TABLE_NAME);
            bean.setId(beanId);
          }
          db.setTransactionSuccessful();
          emitter.onNext(true);
        } finally {
          db.endTransaction();
          emitter.onComplete();
        }
      }
    });
  }

  private int getLastInsertRowId(SQLiteDatabase db, String tableName) {

    Cursor cursor = db.rawQuery("SELECT LAST_INSERT_ROWID() FROM " + tableName, null);
    try {
      if (cursor.moveToFirst()) {
        return cursor.getInt(0);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return -1;
  }
}
