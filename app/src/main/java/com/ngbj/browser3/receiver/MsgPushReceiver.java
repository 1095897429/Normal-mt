package com.ngbj.browser3.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ngbj.browser3.service.MsgPushService;
import com.socks.library.KLog;

public class MsgPushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.d("onReceiveonReceiveonReceive");
        Intent service = new Intent(context, MsgPushService.class);
        context.startService(service);
    }

}
