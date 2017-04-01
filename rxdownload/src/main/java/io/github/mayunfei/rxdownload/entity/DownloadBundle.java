package io.github.mayunfei.rxdownload.entity;

import android.content.ContentValues;
import android.database.Cursor;
import io.github.mayunfei.rxdownload.db.DBHelper;
import java.security.Key;
import java.util.List;

import static io.github.mayunfei.rxdownload.entity.DownloadStatus.DOWNLOADING;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.ERROR;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.FINISH;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.PAUSE;
import static io.github.mayunfei.rxdownload.entity.DownloadStatus.QUEUE;

/**
 * 下载组合
 * Created by yunfei on 17-3-25.
 */

public class DownloadBundle {
  public static final String TABLE_NAME = "DownloadBundle";
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String TOTAL_SIZE = "totalSize";
  public static final String COMPLETED_SIZE = "completedSize";
  public static final String STATUS = "status";
  //分类存储
  public static final String TYPE = "type";
  public static final String ARGS0 = "args0";
  public static final String ARGS1 = "args1";
  public static final String ARGS2 = "args2";
  public static final String ARGS3 = "args3";

  public static final String WHERE0 = "where0";
  public static final String WHERE1 = "where1";


  public static final String CREATE_TABLE = "CREATE TABLE "
      + TABLE_NAME
      + " ("
      + ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + KEY
      + " TEXT NOT NULL,"
      + TOTAL_SIZE
      + " LONG,"
      + COMPLETED_SIZE
      + " LONG,"
      + STATUS
      + " INTEGER,"
      + ARGS0
      + " TEXT,"
      + ARGS1
      + " TEXT,"
      + ARGS2
      + " TEXT,"
      + ARGS3
      + " TEXT,"
      + WHERE0
      + " TEXT,"
      + WHERE1
      + " TEXT,"
      + TYPE
      + " INTEGER"
      + ")";

  public static ContentValues insert(DownloadBundle downloadBundle) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(KEY, downloadBundle.getKey());
    contentValues.put(TOTAL_SIZE, downloadBundle.getTotalSize());
    contentValues.put(COMPLETED_SIZE, downloadBundle.getCompletedSize());
    contentValues.put(STATUS, downloadBundle.getStatus());
    contentValues.put(TYPE, downloadBundle.getType());
    contentValues.put(ARGS0, downloadBundle.getArgs0());
    contentValues.put(ARGS1, downloadBundle.getArgs1());
    contentValues.put(ARGS2, downloadBundle.getArgs2());
    contentValues.put(ARGS3, downloadBundle.getArgs3());
    contentValues.put(WHERE0, downloadBundle.getWhere0());
    contentValues.put(WHERE1, downloadBundle.getWhere1());
    return contentValues;
  }

  public static DownloadBundle getDownloadBundle(Cursor cursor) {
    int id = DBHelper.getInt(cursor, ID);
    String key = DBHelper.getString(cursor, KEY);
    long totalSize = DBHelper.getLong(cursor, TOTAL_SIZE);
    long completedSize = DBHelper.getLong(cursor, COMPLETED_SIZE);
    int status = DBHelper.getInt(cursor, STATUS);
    int type = DBHelper.getInt(cursor, TYPE);
    String arg0 = DBHelper.getString(cursor, ARGS0);
    String arg1 = DBHelper.getString(cursor, ARGS1);
    String arg2 = DBHelper.getString(cursor, ARGS2);
    String arg3 = DBHelper.getString(cursor, ARGS3);
    String where0 = DBHelper.getString(cursor, WHERE0);
    String where1 = DBHelper.getString(cursor, WHERE1);
    DownloadBundle downloadBundle = new DownloadBundle();
    downloadBundle.setId(id);
    downloadBundle.setKey(key);
    downloadBundle.setTotalSize(totalSize);
    downloadBundle.setCompletedSize(completedSize);
    downloadBundle.setStatus(status);
    downloadBundle.setType(type);
    downloadBundle.setArgs0(arg0);
    downloadBundle.setArgs0(arg1);
    downloadBundle.setArgs0(arg2);
    downloadBundle.setArgs0(arg3);
    downloadBundle.setWhere0(where0);
    downloadBundle.setWhere1(where1);
    return downloadBundle;
  }

  public static ContentValues update(int status) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(STATUS, status);
    return contentValues;
  }

  public static ContentValues update(DownloadBundle downloadBundle) {
    ContentValues insert = insert(downloadBundle);
    insert.put(ID, downloadBundle.getId());
    return insert;
  }

  private int id;
  private String key;
  private long totalSize;
  private long completedSize;
  private int status;
  private int type;
  private String args0;
  private String args1;
  private String args2;
  private String args3;
  private String where0;
  private String where1;
  private List<DownloadBean> downloadList;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }

  public long getCompletedSize() {
    return completedSize;
  }

  public void setCompletedSize(long completedSize) {
    this.completedSize = completedSize;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getArgs0() {
    return args0;
  }

  public void setArgs0(String args0) {
    this.args0 = args0;
  }

  public String getArgs1() {
    return args1;
  }

  public void setArgs1(String args1) {
    this.args1 = args1;
  }

  public String getArgs2() {
    return args2;
  }

  public void setArgs2(String args2) {
    this.args2 = args2;
  }

  public String getArgs3() {
    return args3;
  }

  public void setArgs3(String args3) {
    this.args3 = args3;
  }

  public String getWhere0() {
    return where0;
  }

  public void setWhere0(String where0) {
    this.where0 = where0;
  }

  public String getWhere1() {
    return where1;
  }

  public void setWhere1(String where1) {
    this.where1 = where1;
  }

  public List<DownloadBean> getDownloadList() {
    return downloadList;
  }

  public void setDownloadList(List<DownloadBean> downloadList) {
    this.downloadList = downloadList;
  }

  @Override public String toString() {
    String showStatus = "";
    switch (status) {
      case DOWNLOADING:
        showStatus = "下载中";
        break;
      case PAUSE:
        showStatus = "暂停";
        break;
      case QUEUE:
        showStatus = "等待中";
        break;
      case FINISH:
        showStatus = "完成";
        break;
      case ERROR:
        showStatus = "错误";
    }

    return "DownloadBundle{"
        + "id = "
        + id
        + " "
        + key
        + '\''
        + ", totalSize="
        + totalSize
        + ", completedSize="
        + completedSize
        + ", status="
        + showStatus
        + ", type="
        + type
        + '}';
  }

  public void init(DownloadBundle oldBundle) {
    this.setId(oldBundle.getId());
    this.setDownloadList(oldBundle.getDownloadList());
    this.setTotalSize(oldBundle.getTotalSize());
    this.setCompletedSize(oldBundle.getCompletedSize());
  }
}
