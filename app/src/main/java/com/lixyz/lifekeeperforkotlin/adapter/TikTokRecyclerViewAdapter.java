package com.lixyz.lifekeeperforkotlin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lixyz.lifekeeperforkotlin.R;
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoBean;
import com.lixyz.lifekeeperforkotlin.utils.JZMediaExoKernel;
import com.lixyz.lifekeeperforkotlin.utils.JzvdStdTikTok;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import cn.jzvd.JZDataSource;
import cn.jzvd.Jzvd;

public class TikTokRecyclerViewAdapter extends RecyclerView.Adapter<TikTokRecyclerViewAdapter.MyViewHolder> {

    private final Context context;

    private final ArrayList<VideoBean> videos;

    public TikTokRecyclerViewAdapter(Context context, ArrayList<VideoBean> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(
                context).inflate(R.layout.item_tiktok, parent,
                false));
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        VideoBean videoBean = videos.get(position);
        String videoUrl = "https://www.li-xyz.com/LifeKeeper/resource/LifeKeeperPhoneVideo/" + videoBean.getVideoUser() + "/videos/" + videoBean.getSourceFileName();
        String coverUrl = "https://www.li-xyz.com/LifeKeeper/resource/LifeKeeperPhoneVideo/" + videoBean.getVideoUser() + "/cover/" + videoBean.getCoverFileName();

        JZDataSource jzDataSource = new JZDataSource(videoUrl,
                videoBean.getOriginalFileName());
        jzDataSource.looping = true;
        holder.jzvdStd.setUp(jzDataSource, Jzvd.SCREEN_NORMAL);
        holder.jzvdStd.setMediaInterface(JZMediaExoKernel.class);
        Glide.with(holder.jzvdStd.getContext()).load(coverUrl).into(holder.jzvdStd.posterImageView);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        JzvdStdTikTok jzvdStd;

        public MyViewHolder(View itemView) {
            super(itemView);
            jzvdStd = itemView.findViewById(R.id.videoplayer);
        }
    }

}