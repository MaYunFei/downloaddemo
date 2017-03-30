package io.github.mayunfei.downloaddemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by mayunfei on 17-3-30.
 */

public class MyToolbar extends Toolbar {
  public MyToolbar(Context context) {
    this(context, null);
  }

  public MyToolbar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MyToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TextView textView = new TextView(context);
    textView.setGravity(Gravity.CENTER);
    textView.setText("yunfei");
    LayoutParams layoutParams =
        new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //LinearLayout.LayoutParams layoutParams =
    //    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
    //        ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
    textView.setLayoutParams(layoutParams);

    TextView textView2 = new TextView(context);
    textView2.setGravity(Gravity.CENTER);
    textView2.setText("yunfei2");
    LayoutParams layoutParams2 =
        new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //LinearLayout.LayoutParams layoutParams =
    //    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
    //        ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams2.gravity = Gravity.RIGHT;
    textView2.setLayoutParams(layoutParams2);

    addView(textView);
    addView(textView2);
  }
}
