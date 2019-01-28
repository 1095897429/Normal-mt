package com.ngbj.browser3.bean;

import java.io.Serializable;
import java.util.List;

/***
 * 1.集合中cate 后台已排序好
 */
public class AdObjectBean implements Serializable{

    private List<AdBean> cate;
    private List<AdBean> cool_site;
    private List<AdBean> banner;
    private List<AdBean> hot_search;

    public List<AdBean> getCate() {
        return cate;
    }

    public void setCate(List<AdBean> cate) {
        this.cate = cate;
    }

    public List<AdBean> getCool_site() {
        return cool_site;
    }

    public void setCool_site(List<AdBean> cool_site) {
        this.cool_site = cool_site;
    }

    public List<AdBean> getBanner() {
        return banner;
    }

    public void setBanner(List<AdBean> banner) {
        this.banner = banner;
    }

    public List<AdBean> getHot_search() {
        return hot_search;
    }

    public void setHot_search(List<AdBean> hot_search) {
        this.hot_search = hot_search;
    }
}
