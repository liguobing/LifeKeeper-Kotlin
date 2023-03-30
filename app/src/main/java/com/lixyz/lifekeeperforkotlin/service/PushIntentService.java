package com.lixyz.lifekeeperforkotlin.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.lixyz.lifekeeperforkotlin.bean.NewResult;
import com.lixyz.lifekeeperforkotlin.utils.Constant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 继承 GTIntentService 接收来自个推的消息，所有消息在主线程中回调，如果注册了该服务，则务必要在 AndroidManifest 中声明，否则无法接受消息
 */
public class PushIntentService extends GTIntentService {

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    /**
     * 此方法用于接收和处理透传消息。透传消息个推只传递数据，不做任何处理，客户端接收到透传消息后需要自己去做后续动作处理，如通知栏展示、弹框等。
     * 如果开发者在客户端将透传消息创建了通知栏展示，建议将展示和点击回执上报给个推。
     */
    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        byte[] payload = msg.getPayload();
        String data = new String(payload);

        //taskid和messageid字段，是用于回执上报的必要参数。详情见下方文档“6.2 上报透传消息的展示和点击数据”
//        String taskid = msg.getTaskId();
//        String messageid = msg.getMessageId();

    }

    private static final String TAG = "TTT";

    // 接收 cid
    @Override
    public void onReceiveClientId(Context context, String clientid) {
        SharedPreferences config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE);
        String userId = config.getString("UserId", null);
        if (userId != null) {
            String pushClientId = config.getString("PushClientId", null);
            if (pushClientId == null || !pushClientId.equals(clientid)) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                        .readTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url(Constant.CLOUD_ADDRESS + "/LifeKeeper/ResetClientId?clientId=" + clientid)
                        .addHeader("content-type", "application/json;charset=utf-8")
                        .addHeader("Token", userId)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        ResponseBody body = response.body();
                        if(body !=null){
                            Gson gson = new Gson();
                            NewResult result = gson.fromJson(body.string(), NewResult.class);
                            if(result.getResult()){
                                SharedPreferences.Editor edit = config.edit();
                                edit.putString("PushClientId",clientid);
                                edit.apply();
                            }
                        }
                    }
                });
            }
        }
    }

    // cid 离线上线通知
    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    // 各种事件处理回执
    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }

    // 通知到达，只有个推通道下发的通知会回调此方法
    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage msg) {
    }

    // 通知点击，只有个推通道下发的通知会回调此方法
    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage msg) {
    }
}