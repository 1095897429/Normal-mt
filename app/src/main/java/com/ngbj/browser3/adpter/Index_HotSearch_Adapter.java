package com.ngbj.browser3.adpter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ngbj.browser3.R;
import com.ngbj.browser3.bean.AdBean;
import com.ngbj.browser3.bean.KeyBean;

import java.util.List;


public class Index_HotSearch_Adapter extends BaseQuickAdapter<AdBean,BaseViewHolder> {

    public Index_HotSearch_Adapter(List<AdBean> data) {
        super(R.layout.index_tag3_item,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AdBean item) {
        helper.setText(R.id.title, "#" + item.getTitle() + "#");

    }
}