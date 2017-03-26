package io.github.mayunfei.rxdownload.db;

import io.github.mayunfei.rxdownload.entity.DownloadBean;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.reactivex.Observable;

/**
 * 数据库接口
 * Created by yunfei on 17-3-25.
 */

public interface IDownloadDB {

  /**
   * 更新 单个下载记录
   */
  Observable<Boolean> updateDownloadBean(DownloadBean bean);

  /**
   * 更新这一组数据
   */
  Observable<Boolean> updateDownloadBundle(DownloadBundle downloadBundle);

  Observable<Boolean> insertDownloadBundle(DownloadBundle downloadBundle);

  void closeDataBase();
}
