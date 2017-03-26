package io.github.mayunfei.rxdownload.entity;

import android.content.ContentValues;
import java.util.List;

/**
 * Created by yunfei on 17-3-25.
 */

public class DownloadBundle {
  public static final String TABLE_NAME = "DownloadBundle";
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String TOTAL_SIZE = "totalSize";
  public static final String COMPLETED_SIZE = "completedSize";
  public static final String STATUS = "status";
  public static final String TYPE = "type";

  public static final String CREAT_TABLE = "CREATE TABLE "
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

  public List<DownloadBean> getDownloadList() {
    return downloadList;
  }

  public void setDownloadList(List<DownloadBean> downloadList) {
    this.downloadList = downloadList;
  }
}
