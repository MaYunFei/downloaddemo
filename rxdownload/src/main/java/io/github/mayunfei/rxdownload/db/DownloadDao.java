package io.github.mayunfei.rxdownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.github.mayunfei.rxdownload.utils.L;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by mayunfei on 17-3-23.
 */

public class DownloadDao implements IDownloadDB {
  private static final String TAG = "SQLLLLLLLLLL";

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

  @Override public void closeDataBase() {
    synchronized (databaseLock) {
      readableDatabase = null;
      writableDatabase = null;
      sqLiteHelper.close();
    }
  }

  @Override public DownloadEvent selectBundleStatus(String key) {
    DownloadEvent downloadEvent = new DownloadEvent();
    Cursor cursor = getReadableDatabase().query(DownloadBundle.TABLE_NAME,
        new String[] { DownloadBundle.TOTAL_SIZE, DownloadBundle.COMPLETED_SIZE },
        DownloadBundle.KEY + "=?", new String[] { key }, null, null, null);

    cursor.moveToFirst();
    if (cursor.getCount() != 0) {
      long total = DBHelper.getLong(cursor, DownloadBundle.TOTAL_SIZE);
      long complete = DBHelper.getLong(cursor, DownloadBundle.COMPLETED_SIZE);
      downloadEvent.setTotalSize(total);
      downloadEvent.setCompletedSize(complete);
    }

    return downloadEvent;
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

  @Override public boolean updateDownloadBean(DownloadBean bean) {
    //L.i(TAG, "updateDownloadBean \n" + bean);
    getWritableDatabase().update(DownloadBean.TABLE_NAME, DownloadBean.update(bean),
        DownloadBean.URL + "=?", new String[] { bean.getUrl() });

    return true;
  }

  @Override public boolean updateDownloadBundle(DownloadBundle downloadBundle) {
    //L.i(TAG, "updateDownloadBundle \n" + downloadBundle);
    getWritableDatabase().update(DownloadBundle.TABLE_NAME, DownloadBundle.update(downloadBundle),
        DownloadBundle.KEY + "=?", new String[] { downloadBundle.getKey() });

    return true;
  }

  @Override public boolean insertDownloadBundle(final DownloadBundle downloadBundle) {
    L.i(TAG, "insertDownloadBundle");
    SQLiteDatabase db = getWritableDatabase();
    try {
      db.beginTransaction();
      long insert =
          db.insert(DownloadBundle.TABLE_NAME, null, DownloadBundle.insert(downloadBundle));
      if (insert <= 0) {
        return false;
      }
      int bundleId = getLastInsertRowId(db, DownloadBundle.TABLE_NAME);
      if (bundleId == -1) {
        return false;
      }

      for (DownloadBean bean : downloadBundle.getDownloadList()) {
        bean.setBundleId(bundleId);
        db.insert(DownloadBean.TABLE_NAME, null, DownloadBean.insert(bean));
        int beanId = getLastInsertRowId(db, DownloadBean.TABLE_NAME);
        bean.setId(beanId);
      }
      db.setTransactionSuccessful();
      return true;
    } finally {
      db.endTransaction();
    }
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

  @Override public boolean existsDownloadBundle(String key) {
    Cursor cursor = null;
    try {
      cursor =
          getReadableDatabase().query(DownloadBundle.TABLE_NAME, new String[] { DownloadBundle.ID },
              DownloadBundle.KEY + "=?", new String[] { key }, null, null, null);
      cursor.moveToFirst();
      return cursor.getCount() != 0;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }
}
