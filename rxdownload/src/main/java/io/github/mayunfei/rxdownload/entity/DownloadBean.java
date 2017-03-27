package io.github.mayunfei.rxdownload.entity;

import android.content.ContentValues;

/**
 * Created by yunfei on 17-3-25.
 */

public class DownloadBean {

  public static final String TABLE_NAME = "DownloadBean";
  public static final String ID = "id";
  public static final String BUNDLE_ID = "bundleId";
  public static final String FILENAME = "fineName";
  public static final String PATH = "path";
  public static final String TOTAL_SIZE = "totalSize";
  public static final String COMPLETED_SIZE = "completedSize";
  public static final String URL = "url";
  public static final String CREATE_TABLE = "CREATE TABLE "
      + TABLE_NAME
      + " ("
      + ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + BUNDLE_ID
      + " INTEGER,"
      + FILENAME
      + " TEXT NOT NULL,"
      + PATH
      + " TEXT,"
      + TOTAL_SIZE
      + " LONG,"
      + COMPLETED_SIZE
      + " LONG,"
      + URL
      + " TEXT,"
      + "FOREIGN KEY ("
      + BUNDLE_ID
      + ") REFERENCES "
      + DownloadBundle.TABLE_NAME
      + "("
      + DownloadBundle.ID
      + "))";

  public static ContentValues insert(DownloadBean downloadBean) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(BUNDLE_ID, downloadBean.getBundleId());
    contentValues.put(FILENAME, downloadBean.getFileName());
    contentValues.put(PATH, downloadBean.getPath());
    contentValues.put(TOTAL_SIZE, downloadBean.getTotalSize());
    contentValues.put(COMPLETED_SIZE, downloadBean.getCompletedSize());
    contentValues.put(URL, downloadBean.getUrl());
    return contentValues;
  }

  public static ContentValues update(DownloadBean downloadBean) {
    ContentValues insert = insert(downloadBean);
    insert.put(ID, downloadBean.getId());
    return insert;
  }

  private int id;
  private int bundleId;
  private String fileName;
  private String path;
  private long totalSize = -1;
  private long completedSize = 0;
  private String url;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBundleId() {
    return bundleId;
  }

  public void setBundleId(int bundleId) {
    this.bundleId = bundleId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override public String toString() {
    return "DownloadBean{"
        + "id="
        + id
        + ", bundleId="
        + bundleId
        + ", fileName='"
        + fileName
        + '\''
        + ", path='"
        + path
        + '\''
        + ", totalSize="
        + totalSize
        + ", completedSize="
        + completedSize
        + ", url='"
        + url
        + '\''
        + '}';
  }
}
