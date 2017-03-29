package io.github.mayunfei.downloaddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import io.github.mayunfei.rxdownload.RxDownloadManager;
import io.github.mayunfei.rxdownload.entity.DownloadBundle;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;
import kale.adapter.CommonRcvAdapter;
import kale.adapter.item.AdapterItem;

public class DownloadListActivity extends AppCompatActivity {
  private List<DownloadItem> downloadItems;
  private Disposable disposable;
  private RecyclerView recyclerView;
  private CommonRcvAdapter<DownloadItem> mAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_download_list);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    layoutManager.setRecycleChildrenOnDetach(true);
    recyclerView.setLayoutManager(layoutManager);
    downloadItems = new ArrayList<>();
    mAdapter = new CommonRcvAdapter<DownloadItem>(downloadItems) {
      public AdapterItem createItem(Object type) {
        return new DownloadAdapterItem();
      }
    };
    recyclerView.setAdapter(mAdapter);
    disposable = RxDownloadManager.getInstance()
        .getAllDownloadBundle()
        .flatMap(new Function<List<DownloadBundle>, ObservableSource<DownloadBundle>>() {
          @Override public ObservableSource<DownloadBundle> apply(
              @NonNull List<DownloadBundle> downloadBundles) throws Exception {
            return Observable.fromIterable(downloadBundles);
          }
        })
        .map(new Function<DownloadBundle, DownloadItem>() {
          @Override public DownloadItem apply(@NonNull DownloadBundle downloadBundle)
              throws Exception {
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setDownloadBundle(downloadBundle);
            return downloadItem;
          }
        })
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<DownloadItem>>() {
          @Override public void accept(@NonNull List<DownloadItem> tiems) throws Exception {
            downloadItems.clear();
            downloadItems.addAll(tiems);
            recyclerView.getAdapter().notifyDataSetChanged();
          }
        });


    //.subscribe(new Consumer<List<DownloadBundle>>() {
    //  @Override public void accept(@NonNull List<DownloadBundle> downloadBundles)
    //      throws Exception {
    //
    //  }
    //}, new Consumer<Throwable>() {
    //  @Override public void accept(@NonNull Throwable throwable) throws Exception {
    //
    //  }
    //});
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
