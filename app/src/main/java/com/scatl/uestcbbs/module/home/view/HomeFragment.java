package com.scatl.uestcbbs.module.home.view;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.appbar.AppBarLayout;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BaseFragment;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.callback.OnRefresh;
import com.scatl.uestcbbs.custom.MyLinearLayoutManger;
import com.scatl.uestcbbs.entity.BingPicBean;
import com.scatl.uestcbbs.entity.NoticeBean;
import com.scatl.uestcbbs.entity.SimplePostListBean;
import com.scatl.uestcbbs.helper.glidehelper.GlideLoader4Banner;
import com.scatl.uestcbbs.module.board.view.BoardActivity;
import com.scatl.uestcbbs.module.board.view.SingleBoardActivity;
import com.scatl.uestcbbs.module.dayquestion.view.DayQuestionActivity;
import com.scatl.uestcbbs.module.home.adapter.HomeAdapter;
import com.scatl.uestcbbs.module.home.presenter.HomePresenter;
import com.scatl.uestcbbs.module.houqin.view.HouQinReportListActivity;
import com.scatl.uestcbbs.module.magic.view.MagicShopActivity;
import com.scatl.uestcbbs.module.medal.view.MedalCenterActivity;
import com.scatl.uestcbbs.module.post.view.PostDetailActivity;
import com.scatl.uestcbbs.module.user.view.UserDetailActivity;
import com.scatl.uestcbbs.module.webview.view.WebViewActivity;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.FileUtil;
import com.scatl.uestcbbs.util.RefreshUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author: sca_tl
 * description: 首页最新发表
 */
public class HomeFragment extends BaseFragment implements HomeView {

    private static final String TAG = "HomeFragment";

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;

    private View bannerView, noticeView, hotPostView, gongGeView;
    private CardView timetableCard, lostAndFoundCard, hotPostCard, noticeCard,
            rankCard, onLineUserCard, medalCenterCard, houQinReportCard, magicShopCard;
    private Banner banner;
    private TextView noticeContent;

    private HomePresenter homePresenter;

    public static final int DOWNLOAD_PIC = 22;
    private String imgUrl, imgCopyRight;

    private int total_post_page = 1;

    public static HomeFragment getInstance(Bundle bundle) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        return homeFragment;
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void findView() {

        homePresenter = (HomePresenter) presenter;
        appBarLayout = view.findViewById(R.id.home_app_bar);
        toolbar = view.findViewById(R.id.home_toolbar);
        recyclerView = view.findViewById(R.id.home_rv);
        refreshLayout = view.findViewById(R.id.home_refresh);

        bannerView = LayoutInflater.from(mActivity).inflate(R.layout.home_item_banner_view, new LinearLayout(mActivity));
        banner = bannerView.findViewById(R.id.home_item_banner_view_banner);

        gongGeView = LayoutInflater.from(mActivity).inflate(R.layout.home_item_gongge_view, new LinearLayout(mActivity));
        timetableCard = gongGeView.findViewById(R.id.home_item_gongge_view_timetable_card);
        lostAndFoundCard = gongGeView.findViewById(R.id.home_item_gongge_view_lost_and_found_card);
        hotPostCard = gongGeView.findViewById(R.id.home_item_gongge_view_hot_post_card);
        rankCard = gongGeView.findViewById(R.id.home_item_gongge_view_department_card);
        onLineUserCard = gongGeView.findViewById(R.id.home_item_gongge_view_online_user_card);
        medalCenterCard = gongGeView.findViewById(R.id.home_item_gongge_view_medal_center_card);
        houQinReportCard = gongGeView.findViewById(R.id.home_item_gongge_view_houqin_report_card);
        magicShopCard = gongGeView.findViewById(R.id.home_item_gongge_view_magic_shop_card);

        noticeView = LayoutInflater.from(mActivity).inflate(R.layout.home_item_notice_view, new LinearLayout(mActivity));
        noticeContent = noticeView.findViewById(R.id.home_item_notice_view_content);
        noticeCard = noticeView.findViewById(R.id.home_item_notice_view_card);
    }

    @Override
    protected void initView() {

        homeAdapter = new HomeAdapter(R.layout.item_simple_post);

        toolbar.setOnClickListener(this::onClickListener);
        lostAndFoundCard.setOnClickListener(this::onClickListener);
        timetableCard.setOnClickListener(this::onClickListener);
        hotPostCard.setOnClickListener(this::onClickListener);
        rankCard.setOnClickListener(this::onClickListener);
        onLineUserCard.setOnClickListener(this::onClickListener);
        medalCenterCard.setOnClickListener(this::onClickListener);
        houQinReportCard.setOnClickListener(this::onClickListener);
        magicShopCard.setOnClickListener(this::onClickListener);

        homeAdapter.addHeaderView(bannerView, 0);
        homeAdapter.addHeaderView(noticeView, 1);
        homeAdapter.addHeaderView(gongGeView, 2);

        recyclerView.setLayoutManager(new MyLinearLayoutManger(mActivity));
        recyclerView.setAdapter(homeAdapter);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mActivity, R.anim.layout_animation_scale_in));
        recyclerView.scheduleLayoutAnimation();

        initSavedData();

    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        refreshLayout.autoRefresh(0, 300, 1, false);
    }

    @Override
    protected BasePresenter initPresenter() {
        return new HomePresenter();
    }

    private long t = 0;
    @Override
    protected void onClickListener(View v) {
        if (v.getId() == R.id.home_toolbar) {
            //双击标题栏快速回顶
            if (System.currentTimeMillis() - t > 300) {
                t = System.currentTimeMillis();
            } else {
                recyclerView.scrollToPosition(0);
            }
        }
        if (v.getId() == R.id.home_item_gongge_view_lost_and_found_card){
            Intent intent = new Intent(mActivity, SingleBoardActivity.class);
            intent.putExtra(Constant.IntentKey.BOARD_ID, 305);
            startActivity(intent);
        }
        if (v.getId() == R.id.home_item_gongge_view_timetable_card) {
            Intent intent = new Intent(mActivity, WebViewActivity.class);
            intent.putExtra(Constant.IntentKey.URL, Constant.BUS_TIME);
            startActivity(intent);
        }
        if (v.getId() == R.id.home_item_gongge_view_hot_post_card){
            startActivity(new Intent(mActivity, DayQuestionActivity.class));
        }

        if (v.getId() == R.id.home_item_gongge_view_department_card){
            Intent intent = new Intent(mActivity, BoardActivity.class);
            intent.putExtra(Constant.IntentKey.BOARD_ID, Constant.DEPARTMENT_BOARD_ID);
            intent.putExtra(Constant.IntentKey.BOARD_NAME, Constant.DEPARTMENT_BOARD_NAME);
            startActivity(intent);
        }
        if (v.getId() == R.id.home_item_gongge_view_online_user_card) {
            OnLineUserFragment.getInstance(null).show(getChildFragmentManager(), TimeUtil.getStringMs());
        }
        if (v.getId() == R.id.home_item_gongge_view_medal_center_card) {
            startActivity(new Intent(mActivity, MedalCenterActivity.class));
        }
        if (v.getId() == R.id.home_item_gongge_view_houqin_report_card) {
            startActivity(new Intent(mActivity, HouQinReportListActivity.class));
        }
        if (v.getId() == R.id.home_item_gongge_view_magic_shop_card) {
            startActivity(new Intent(mActivity, MagicShopActivity.class));
        }
    }

    /**
     * author: sca_tl
     * description: 展示已保存的数据
     */
    private void initSavedData() {
        String homeBannerData = FileUtil.readTextFile(
                new File(mActivity.getExternalFilesDir(Constant.AppPath.JSON_PATH),
                        Constant.FileName.HOME_BANNER_JSON));
        if (JSON.isValidObject(homeBannerData)) {
            JSONObject jsonObject = JSONObject.parseObject(homeBannerData);
            BingPicBean bingPicBean = JSON.toJavaObject(jsonObject, BingPicBean.class);
            initBannerView(bingPicBean);
        }

        String homeSimplePostData = FileUtil.readTextFile(
                new File(mActivity.getExternalFilesDir(Constant.AppPath.JSON_PATH),
                        Constant.FileName.HOME_SIMPLE_POST_JSON));
        if (JSON.isValidObject(homeSimplePostData)) {
            JSONObject jsonObject = JSONObject.parseObject(homeSimplePostData);
            SimplePostListBean simplePostListBean = JSON.toJavaObject(jsonObject, SimplePostListBean.class);
            homeAdapter.addData(simplePostListBean.list, true);
        }
    }

    @Override
    protected void setOnItemClickListener() {
        homeAdapter.setOnItemClickListener((adapter, view1, position) -> {
            if (view1.getId() == R.id.item_simple_post_card_view) {
                Intent intent = new Intent(mActivity, PostDetailActivity.class);
                intent.putExtra(Constant.IntentKey.TOPIC_ID, homeAdapter.getData().get(position).topic_id);
                startActivity(intent);
            }
        });

        homeAdapter.setOnItemChildClickListener((adapter, view1, position) -> {
            if (view1.getId() == R.id.item_simple_post_board_name) {
                Intent intent = new Intent(mActivity, SingleBoardActivity.class);
                intent.putExtra(Constant.IntentKey.BOARD_ID, homeAdapter.getData().get(position).board_id);
                startActivity(intent);
            }
            if (view1.getId() == R.id.item_simple_post_user_avatar) {
                Intent intent = new Intent(mActivity, UserDetailActivity.class);
                intent.putExtra(Constant.IntentKey.USER_ID, homeAdapter.getData().get(position).user_id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void setOnRefreshListener() {
        RefreshUtil.setOnRefreshListener(mActivity, refreshLayout, new OnRefresh() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                total_post_page = 1;

                homePresenter.getHomePage();
                homePresenter.getBannerData();
                homePresenter.getNotice();
                homePresenter.getSimplePostList(1, SharePrefUtil.getPageSize(mActivity), "new", mActivity);
            }

            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                homePresenter.getSimplePostList(total_post_page, SharePrefUtil.getPageSize(mActivity), "new", mActivity);
            }
        });
    }

    /**
     * author: sca_tl
     * description: banner
     */
    private void initBannerView(BingPicBean bingPicBean) {
        List<String> imgUrls = new ArrayList<>();
        List<String> imgTitles = new ArrayList<>();

        for (int i = 0; i < bingPicBean.images.size(); i ++) {
            imgUrls.add(ApiConstant.BING_BASE_URL + bingPicBean.images.get(i).url);
            imgTitles.add(bingPicBean.images.get(i).copyright);
        }

        banner
                .setImages(imgUrls)
                .setBannerTitles(imgTitles)
                .setImageLoader(new GlideLoader4Banner())
                .setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE)
                .setDelayTime(3000)
                .setOnBannerListener(position -> {
                    this.imgUrl = imgUrls.get(position);
                    this.imgCopyRight = imgTitles.get(position);
                    homePresenter.downDailyPicConfirm(getActivity());
                })
                .start();
            bannerView.setVisibility(SharePrefUtil.isShowHomeBanner(mActivity) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void getBannerDataSuccess(BingPicBean bingPicBean) {
        initBannerView(bingPicBean);

        //保存为json文件
        FileUtil.saveStringToFile(JSON.toJSONString(bingPicBean),
                new File(mActivity.getExternalFilesDir(Constant.AppPath.JSON_PATH),
                        Constant.FileName.HOME_BANNER_JSON));
    }

    @Override
    public void getSimplePostDataSuccess(SimplePostListBean simplePostListBean) {
        total_post_page = total_post_page + 1;

        homeAdapter.addData(simplePostListBean.list, refreshLayout.getState() == RefreshState.Refreshing);

        if (refreshLayout.getState() == RefreshState.Refreshing)
            refreshLayout.finishRefresh();
        if (refreshLayout.getState() == RefreshState.Loading) {
            refreshLayout.finishLoadMore();
        }

        if (simplePostListBean.page == 1) {
            //保存为json文件
            FileUtil.saveStringToFile(JSON.toJSONString(simplePostListBean),
                    new File(mActivity.getExternalFilesDir(Constant.AppPath.JSON_PATH),
                            Constant.FileName.HOME_SIMPLE_POST_JSON));
        }
    }

    @Override
    public void getSimplePostDataError(String msg) {
        if (refreshLayout.getState() == RefreshState.Refreshing) {
            refreshLayout.finishRefresh();
        }
        if (refreshLayout.getState() == RefreshState.Loading) {
            refreshLayout.finishLoadMore(false);
        }

        showSnackBar(mActivity.getWindow().getDecorView(), msg);
    }

    @Override
    public void onGetNoticeSuccess(NoticeBean noticeBean) {
        if (noticeBean.isValid) {
            noticeCard.setVisibility(View.VISIBLE);
            noticeContent.setText(noticeBean.content);
        }
    }

    @Override
    public void onGetNoticeError(String msg) {
        noticeCard.setVisibility(View.GONE);
    }

    @Override
    public void onGetHomePageSuccess(String msg) {

    }

    @Override
    public void onPermissionGranted(int action) {
        if (action == DOWNLOAD_PIC) {
            showSnackBar(mActivity.getWindow().getDecorView(), "正在下载，您可到系统下载管理或者Download文件夹查看下载的文件");
            CommonUtil.download(mActivity, this.imgUrl, this.imgCopyRight.replace("/", "_") + ".jpg");
        }
    }

    @Override
    public void onPermissionRefused() {
        showSnackBar(mActivity.getWindow().getDecorView(), getString(R.string.permission_request));
    }

    @Override
    public void onPermissionRefusedWithNoMoreRequest() {
        showSnackBar(mActivity.getWindow().getDecorView(), getString(R.string.permission_refuse));
    }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void onEventBusReceived(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.HOME_REFRESH ||
                baseEvent.eventCode == BaseEvent.EventCode.HOME_BANNER_VISIBILITY_CHANGE) {
            recyclerView.scrollToPosition(0);
            refreshLayout.autoRefresh(0, 300, 1, false);
        }
    }


}
