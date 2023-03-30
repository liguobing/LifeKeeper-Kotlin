package com.lixyz.lifekeeperforkotlin.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.lixyz.lifekeeperforkotlin.R;
import com.lixyz.lifekeeperforkotlin.adapter.TikTokRecyclerViewAdapter;
import com.lixyz.lifekeeperforkotlin.base.BaseActivity;
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoBean;
import com.lixyz.lifekeeperforkotlin.presenter.TiktokPresenter;
import com.lixyz.lifekeeperforkotlin.utils.JzvdStdTikTok;
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import cn.jzvd.Jzvd;

public class TikTokActivity extends BaseActivity implements ITiktokView {

    private RecyclerView rvTiktok;
    private TikTokRecyclerViewAdapter mAdapter;
    private int mCurrentPosition = -1;
    private ArrayList<VideoBean> videos = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 通过 SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 将布局设置为全屏显示
        View decorView1 = getWindow().getDecorView();
        int full = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView1.setSystemUiVisibility(full);
        // StatusBar 设置为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_tiktok);

        initWidget();
        initListener();

    }


    private void autoPlayVideo() {
        if (rvTiktok == null || rvTiktok.getChildAt(0) == null) {
            return;
        }
        JzvdStdTikTok player = rvTiktok.getChildAt(0).findViewById(R.id.videoplayer);
        if (player != null) {
            player.startVideoAfterPreloading();
        }
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateRecyclerView(ArrayList<VideoBean> list) {
        this.videos.clear();
        this.videos.addAll(list);
        runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            rvTiktok.scrollToPosition(position);
        });

    }

    @Override
    public void showWaitDialog() {
        if (!waitDialog.isShowing()) {
            waitDialog.show();
        }
    }

    @Override
    public void hideWaitDialog() {
        runOnUiThread(() -> {
            if (waitDialog.isShowing()) {
                waitDialog.dismiss();
            }
        });
    }

    @Override
    public void showSnackBar(String msg) {
        runOnUiThread(() -> Snackbar.make(rvTiktok, msg, Snackbar.LENGTH_SHORT).show());
    }

    private ViewPagerLayoutManager mViewPagerLayoutManager;
    private CustomDialog waitDialog;
    private int position = 0;

    @Override
    public void initWidget() {
        waitDialog = new CustomDialog(this, this, "数据载入中，请稍候...");
        String categoryId = getIntent().getStringExtra("CategoryId");
        String password = getIntent().getStringExtra("Password");
        position = getIntent().getIntExtra("Position", 0);
        rvTiktok = findViewById(R.id.rv_tiktok);
        mAdapter = new TikTokRecyclerViewAdapter(this, videos);
        mViewPagerLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        rvTiktok.setLayoutManager(mViewPagerLayoutManager);
        rvTiktok.setAdapter(mAdapter);
        TiktokPresenter tiktokPresenter = new TiktokPresenter(this, this);
        assert categoryId != null;
        assert password != null;
        tiktokPresenter.getVideos(this, categoryId, password);
    }

    @Override
    public void initListener() {
        mViewPagerLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                //自动播放第一条
                autoPlayVideo();
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                if (mCurrentPosition == position) {
                    Jzvd.releaseAllVideos();
                }
            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                if (mCurrentPosition == position) {
                    return;
                }
                autoPlayVideo();
                mCurrentPosition = position;
            }
        });

        rvTiktok.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NotNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NotNull View view) {
                Jzvd jzvd = view.findViewById(R.id.videoplayer);
                if (jzvd != null && Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource != null &&
                        jzvd.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.getCurrentUrl())) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos();
                    }
                }
            }
        });
    }
}
