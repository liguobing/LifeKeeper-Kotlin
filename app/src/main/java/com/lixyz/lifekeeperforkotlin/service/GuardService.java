package com.lixyz.lifekeeperforkotlin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lixyz.lifekeeperforkotlin.R;

public class GuardService extends Service {
    private static final String TAG = "TTT";

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private static final String NORMAL_CHANNEL_ID = "my_notification_normal";
    private static final String IMPORTANT_CHANNEL_ID = "my_notification_important";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder mBuilder = new Notification.Builder(getApplicationContext(),"channelId").setAutoCancel(true);// 点击后让通知将消失
        mBuilder.setContentTitle("电子管家正在监听服务器的推送");
        mBuilder.setContentText("该功能不会明显增加耗电，无须担心");
        mBuilder.setSmallIcon(R.mipmap.logo);
        mBuilder.setWhen(System.currentTimeMillis());//通知产生的时间，会在通知信息里显示
        mBuilder.setOngoing(false);//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "channelId";
        NotificationChannel channel = new NotificationChannel(channelId, getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);
        mBuilder.setContentIntent(null);
        startForeground(222, mBuilder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
