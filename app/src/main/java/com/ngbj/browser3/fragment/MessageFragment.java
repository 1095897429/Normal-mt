package com.ngbj.browser3.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ngbj.browser3.R;
import com.ngbj.browser3.activity.WebViewByNewsActivity;
import com.ngbj.browser3.adpter.NewsMultiAdapter;
import com.ngbj.browser3.bean.HistoryData;
import com.ngbj.browser3.bean.NewsBean;
import com.ngbj.browser3.bean.NewsSaveMultiBean;
import com.ngbj.browser3.constant.ApiConstants;
import com.ngbj.browser3.db.DBManager;
import com.ngbj.browser3.event.DataToTopEvent;
import com.ngbj.browser3.event.NewsShowFragmentEvent;
import com.ngbj.browser3.event.RefreshDataEvent;
import com.ngbj.browser3.network.retrofit.utils.Sha1SignUtils;
import com.ngbj.browser3.util.DeviceIdHepler;
import com.ngbj.browser3.util.SPHelper;
import com.ngbj.browser3.util.StringUtils;
import com.ngbj.browser3.util.ToastUtil;
import com.ngbj.browser3.view.CustomLoadMoreView;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Date:2018/8/20
 * author:zl
 * 备注：
 */
public class MessageFragment extends BaseFragment {

    public static MessageFragment getInstance(){
        return new MessageFragment();
    }


    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;
    private List<NewsSaveMultiBean> mList = new ArrayList<>();
    NewsMultiAdapter myRecyclerAdapter;
    DBManager dbManager;

    LinearLayoutManager layoutManager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    //手动刷新新闻列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataToTopEvent(DataToTopEvent event) {
        KLog.d("CustomerFragment");
//        if(event.getIndex() == 2){
//            layoutManager.scrollToPositionWithOffset(0, 0);
//        }
    }

    //刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshDataEvent(RefreshDataEvent event) {
        isFirst = true;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void initVidget() {
        dbManager = DBManager.getInstance(getActivity());
        initRecycleView();
        initEvent();
        initMoreData();
        initSwipeRefreshLayout();
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light);
        //给swipeRefreshLayout绑定刷新监听
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //设置2秒的时间来执行以下事件
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //TODO 请求数据
                        KLog.d(" 请求数据");
                        getNews3Data();
//                        update_text.setVisibility(View.VISIBLE);

//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                update_text.setVisibility(View.GONE);
//                            }
//                        },2000);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

    }





    //加载更多数据
    private void initMoreData() {
        myRecyclerAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getMoreNewsData();
            }
        },recyclerView);
    }


    private void getMoreNewsData() {
        long time = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();
        map.put("num","20");
        map.put("chaid","0");
        //map.put("from","browse");//它需要统计来源
        map.put("device_serial", DeviceIdHepler.getUniquePsuedoID());
        map.put("app_key", "llq2db90");
        map.put("auth_timestamp", time);
        map = Sha1SignUtils.reSign(map);


        //获取到签名文件
        String qianming = (String) map.get("auth_signature");

        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        //post方式提交的数据
        FormBody formBody = new FormBody.Builder()
                .add("num", "20")
                .add("chaid", "0")
                .add("device_serial",  DeviceIdHepler.getUniquePsuedoID())
                .add("app_key", "llq2db90")
                .add("auth_timestamp", String.valueOf(time))
                .add("auth_signature",qianming)
                .build();

        final Request request = new Request.Builder()
                .url(ApiConstants.BASEURL)//请求的url
                .post(formBody)
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                KLog.d("连接失败");

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myRecyclerAdapter.loadMoreFail();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    String result = response.body().string();
                    KLog.d(result);
                    final NewsBean newsBean = JSONObject.parseObject(result,NewsBean.class);
                    if(newsBean.getReturn_code().equals("200")){
                        KLog.d("推荐数量：" + newsBean.getReturn_data().getCom_list().size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                myRecyclerAdapter.loadMoreComplete();

                                List<NewsSaveMultiBean> saveBeanList = transToList(newsBean);
                                myRecyclerAdapter.addData(saveBeanList);
//                                dbManager.insertNewsList(saveBeanList);



                            }
                        });
                    }

                }
            }
        });

    }





    private void initRecycleView() {
         layoutManager = new LinearLayoutManager(getActivity() );
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //设置Adapter
        myRecyclerAdapter = new NewsMultiAdapter(mList);
        recyclerView.setAdapter(myRecyclerAdapter);
//        recyclerView.setNestedScrollingEnabled(false);// 解决滑动冲突

        //设置自定义加载布局
        myRecyclerAdapter.setLoadMoreView(new CustomLoadMoreView());
    }

    private void initEvent() {
        //条目点击事件
        myRecyclerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if(StringUtils.isFastClick()){
                    return;
                }

                isNetwork = (boolean) SPHelper.get(getActivity(),"is_network",false);
                if(isNetwork){
                    int myType = getParentFragmentToType(MessageFragment.this);
                    EventBus.getDefault().post(
                            new NewsShowFragmentEvent(myRecyclerAdapter.getData().get(position).getH5url(),myType));
                }else {
                    ToastUtil.showShort(getActivity(),"网络异常");
                    return;
                }


            }
        });

    }



    @Override
    protected void lazyLoadData() {
        //TODO 数据库数据
        isNetwork = (boolean) SPHelper.get(getActivity(),"is_network",false);
        if(isNetwork){
            swipeRefreshLayout.setRefreshing(true);
            getNews3Data();
        }else {
        }
    }


    private void getNews3Data() {
        long time = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();
        map.put("num","20");
        map.put("chaid","0");
        map.put("device_serial", DeviceIdHepler.getUniquePsuedoID());
        map.put("app_key", "llq2db90");
        map.put("auth_timestamp", time);
        map = Sha1SignUtils.reSign(map);

        //获取到签名文件
        String qianming = (String) map.get("auth_signature");

        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        KLog.d("设备Id:" + DeviceIdHepler.getUniquePsuedoID());
        KLog.d("auth_timestamp" + time);
        KLog.d("签名文件" + qianming);
        //post方式提交的数据
        FormBody formBody = new FormBody.Builder()
                .add("num","20")
                .add("chaid","0")
                .add("device_serial",  DeviceIdHepler.getUniquePsuedoID())
                .add("app_key", "llq2db90")
                .add("auth_timestamp", String.valueOf(time))
                .add("auth_signature",qianming)
                .build();

        final Request request = new Request.Builder()
                .url(ApiConstants.BASEURL3)//请求的url
                .post(formBody)
                .build();

        //创建Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                KLog.d("连接失败");
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {

                    String result = response.body().string();
                    KLog.d("result：" + result);
                    final NewsBean newsBean = JSONObject.parseObject(result,NewsBean.class);
                    if(newsBean.getReturn_code().equals("200")){
                        KLog.d("推荐数量：" + newsBean.getReturn_data().getCom_list().size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                List<NewsSaveMultiBean> saveBeanList = transToList(newsBean);
                                myRecyclerAdapter.setNewData(saveBeanList);
                                //TODO 保存到数据库中
                            }
                        });
                    }

                }
            }
        });

    }
}
