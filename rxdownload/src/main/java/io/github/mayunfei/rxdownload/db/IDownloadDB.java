package io.github.mayunfei.rxdownload.db;

import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.github.mayunfei.rxdownload.entity.DownloadEvent;
import io.reactivex.Observable;
import java.util.List;

/**
 * 数据库接口
 * Created by yunfei on 17-3-25.
 */

public interface IDownloadDB {

  /**
   * 更新 单个下载记录
   */
  boolean updateDownloadBean(DownloadBean bean);

  /**
   * 更新这一组数据
   */
  boolean updateDownloadBundle(DownloadBundle downloadBundle);

  boolean insertDownloadBundle(DownloadBundle downloadBundle);

  boolean existsDownloadBundle(String key);

  void closeDataBase();

  /**
   * 查询 下载状态
   */
  DownloadEvent selectBundleStatus(String key);

  List<DownloadBundle> getAllDownloadBundle();

  /**
   * 根据条件查找下载，一般用户名
   */
  List<DownloadBundle> getDownloadBundleByWhere(String where);

  void pauseAll();

  DownloadBundle getDownloadBundle(String key);

  void setBeanFinished(int beanId);

  void delete(String key);
}
