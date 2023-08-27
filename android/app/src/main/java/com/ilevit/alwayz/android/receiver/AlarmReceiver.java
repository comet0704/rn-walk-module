package com.ilevit.alwayz.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ilevit.alwayz.android.service.NotificationStepService;

/**
* 노티피케이션 서비스에서 죽었을때 실행되는 클래스
* @author 장지운
* @version 1.0.0
* @since
**/
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent in = new Intent(context, NotificationStepService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(in);
        } else {
            context.startService(in);
        }
    }
}

