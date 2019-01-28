package com.ngbj.browser3.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mobads.CpuInfoManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ngbj.browser3.R;
import com.ngbj.browser3.activity.DownPicActivity;
import com.ngbj.browser3.activity.SearchActivity;
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
import com.ngbj.browser3.view.CustomDecoration;
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
import butterknife.OnClick;
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
public class  HomeFragment extends BaseFragment {

    @BindView(R.id.baiduWebView)
    WebView mWebView;//新闻

    @BindView(R.id.no_net_ll)
    LinearLayout no_net_ll;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.update_text)
    TextView update_text;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    private List<NewsSaveMultiBean> mList = new ArrayList<>();
    NewsMultiAdapter myRecyclerAdapter;
    DBManager dbManager;

    LinearLayoutManager layoutManager;

    boolean isNotFristLoad = false;



    public static HomeFragment getInstance(){
        return new HomeFragment();
    }



    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        showSelectedCpuWebPage();

    }

    /**
     * 调用SDK接口，获取内容联盟页面URL
     */
    private void showSelectedCpuWebPage() {
        // 内容联盟url获取后只能展示一次，多次展示需要每次通过以下接口重新获取
        // 媒体伙伴必须在MSSP业务端选择接入内容联盟的应用与频道类型，以便在接入内容页中生成广告，从而获得广告收益。
        // 不进行相关操作，将无法获得内容联盟页面的广告收益。
        CpuInfoManager.getCpuInfoUrl(getActivity(), getAppsid(), Index_Fragment.CpuChannel.CHANNEL_ENTERTAINMENT.getValue(), new CpuInfoManager.UrlListener() {

            @Override
            public void onUrl(String url) {
                handleWebViewLayout(url);
            }
        });
    }

    /**
     * 获取appsid
     *
     * @return
     */
    private String getAppsid() {
        return ApiConstants.DEFAULT_APPSID;
    }


    /**
     * 根据内容联盟url，渲染页面
     *
     * @param url
     */
    private void handleWebViewLayout(String url) {
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 如果是图片频道，则必须设置该接口为true，否则页面无法展现
        webSettings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.loadUrl(url);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        KLog.d("HomeFragment销毁");
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataToTopEvent(DataToTopEvent event) {
//        KLog.d("HomeFragment");
        if(event.getIndex() == 0){
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
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
        dbManager  = DBManager.getInstance(getActivity());

//        initRecycleView();
        initEvent();
        initMoreData();
//        initSwipeRefreshLayout();

    }

    //加载更多数据
    private void initMoreData() {
//        myRecyclerAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
//            @Override
//            public void onLoadMoreRequested() {
//                getMoreNewsData();
//            }
//        },recyclerView);
    }

    @Override
    protected void lazyLoadData() {
//        isNetwork = (boolean) SPHelper.get(getActivity(),"is_network",false);
//        if(isNetwork){
//            swipeRefreshLayout.setRefreshing(true);
//            getNewsData();
//        }else{
//            swipeRefreshLayout.setVisibility(View.GONE);
//            no_net_ll.setVisibility(View.VISIBLE);
//        }
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
                        isNotFristLoad = true;
                        getNewsData();
                    }
                }, 2000);
            }
        });

    }


    private void initEvent() {
        //条目点击事件
//        myRecyclerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//
//                if(StringUtils.isFastClick()){
//                    KLog.d("点击的太快啦");
//                    return;
//                }
//
//                isNetwork = (boolean) SPHelper.get(getActivity(),"is_network",false);
//                if(isNetwork){
//                    int myType = getParentFragmentToType(HomeFragment.this);
//                    EventBus.getDefault().post(
//                            new NewsShowFragmentEvent(myRecyclerAdapter.getData().get(position).getH5url(),myType));
//                }else {
//                    ToastUtil.showShort(getActivity(),"网络异常");
//                    return;
//                }
//
//            }
//        });



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
        //下划线
        recyclerView.addItemDecoration(new CustomDecoration(getActivity(),
                CustomDecoration.VERTICAL_LIST,R.drawable.divider,0));
        //一行代码开启动画 默认CUSTOM动画
        myRecyclerAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);

    }


    //为什么不和getNewData一样尼
    private void getMoreNewsData() {
        long time = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();
        map.put("num","20");
        map.put("page","1");
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
                .add("page", "1")
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
                        Toast.makeText(getActivity(),"网络连接失败",Toast.LENGTH_SHORT).show();
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
                            }
                        });
                    }

                }
            }
        });

    }

    private void getNewsData() {
        long time = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();
        map.put("num","20");
        map.put("page","1");
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
                .add("page", "1")
                .add("device_serial",  DeviceIdHepler.getUniquePsuedoID())
                .add("app_key", "llq2db90")
                .add("auth_timestamp", String.valueOf(time))
                .add("auth_signature",qianming)
                .build();

        final Request request = new Request.Builder()
                .url(ApiConstants.BASEURL)//请求的url
                .post(formBody)
                .build();

        //创建Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        KLog.d("连接失败,显示重新加载框");
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setVisibility(View.GONE);
                        no_net_ll.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    String result = response.body().string();
                    final NewsBean newsBean = JSONObject.parseObject(result,NewsBean.class);
                    if(newsBean.getReturn_code().equals("200")){
                        KLog.d("推荐数量：" + newsBean.getReturn_data().getCom_list().size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setRefreshing(false);
                                no_net_ll.setVisibility(View.GONE);
                                List<NewsSaveMultiBean> saveBeanList = transToList(newsBean);
                                if(isNotFristLoad){
                                    myRecyclerAdapter.addData(0,saveBeanList);
                                    myRecyclerAdapter.notifyDataSetChanged();
                                }else
                                    myRecyclerAdapter.setNewData(saveBeanList);

                                isNotFristLoad = false;
                            }
                        });
                    }

                }
            }
        });
    }


    @OnClick(R.id.load_new)
    public void LoadNew(){
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNewsData();
            }
        }).start();

    }

}
