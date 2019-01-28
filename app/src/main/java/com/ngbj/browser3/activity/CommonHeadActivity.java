package com.ngbj.browser3.activity;

import android.widget.TextView;

import com.ngbj.browser3.R;
import com.ngbj.browser3.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class CommonHeadActivity extends BaseActivity {

    @BindView(R.id.center_title)
    TextView center_title;

   @OnClick(R.id.back)
    public void back(){
       finish();
   }
}
