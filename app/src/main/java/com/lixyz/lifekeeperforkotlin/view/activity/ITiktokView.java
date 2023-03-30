package com.lixyz.lifekeeperforkotlin.view.activity;

import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoBean;

import java.util.ArrayList;

public interface ITiktokView {
    void updateRecyclerView(ArrayList<VideoBean> list);

    void showWaitDialog();

    void hideWaitDialog();

    void showSnackBar(String msg);
}
