package com.ilevit.alwayz.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import com.ilevit.alwayz.android.service.NotificationStepService;
import com.ilevit.alwayz.android.util.Global;


/**
 * 단말기 재부팅 실행
 *
 * @author 장지운
 * @version 1.0.0
 * @since
 **/
public class RebootRecever extends BroadcastReceiver {

    private static final String TAG = RebootRecever.class.getSimpleName();
    private Handler mHandler = null;
    private Runnable mRunnable = null;

    /**
     * //BroadCast를 받앗을때 자동으로 호출되는 콜백 메소드
     * //첫번째 파라미터 : Context 컨텍스트
     * //두번째 파라미터 : BroadCast의 정보를 가지고 있는 Intent
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

//        Log.d(TAG, "========================시스템 리붓 리시버 수신========================");


        //수신받은 방송(Broadcast)의 Action을 얻어오기
        //메니페스트 파일안에 이 Receiver에게 적용된 필터(Filter)의 Action만 받아오게 되어 있음.
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
//            Log.d(TAG, "========================시스템 리붓 리시버 수신 android.intent.action.BOOT_COMPLETED 속에 들어옴========================");

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    // Start Step Counter service
                    Global.gContext = context;
                    Intent myIntent = new Intent(context, NotificationStepService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(myIntent);
                    } else {
                        context.startService(myIntent);
                    }

                }
            };
            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, 3000);

//            WritableMap params = Arguments.createMap();
//            params.putString("msg", "rebootDone");
//
//            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("calledFromNative", params);

        }


    }
}






