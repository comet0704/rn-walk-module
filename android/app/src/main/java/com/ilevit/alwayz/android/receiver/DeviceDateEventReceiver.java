package com.ilevit.alwayz.android.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ilevit.alwayz.android.net.retrofit.RetrofitCallBack;
import com.ilevit.alwayz.android.net.retrofit.RetrofitClient;
import com.ilevit.alwayz.android.net.retrofit.model.StepDataModel;
import com.ilevit.alwayz.android.net.retrofit.service.StepService;
import com.ilevit.alwayz.android.util.Global_UTIL;
import com.ilevit.alwayz.android.util.Preferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * 자정 되었을때 알려주는 알람
 *
 * @author 장지운
 * @version 1.0.0
 * @since
 **/
public class DeviceDateEventReceiver extends BroadcastReceiver {

    private static final String TAG = "DeviceDateEventReceiver";

    private Context mContext = null;

    private Retrofit retrofitClient = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        Global_UTIL.file_log(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$자정 DeviceDateEventReceiver onReceive 이벤트 발생$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ START");
//        Log.d(TAG, "onReceive : " + action);

        mContext = context;

        if (retrofitClient == null) {
            retrofitClient = RetrofitClient.getInstance().getRetrofit();
        }

        /** 자정이 아닌 정오에도 둘다 발생하는 단말기가 있다,
         * 그래서 현재 날짜를 가져와서 오전 인지 오후인지 판별하고 오후이면 아래 초기화를 진행하지 않고, 자정 알람 셑만 다시 한다.
         */
        // 오전/오후 구하기
        Calendar calendar = Calendar.getInstance();
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));

        Calendar now = Calendar.getInstance();
        int ampm = calendar.get(calendar.AM_PM);
        String strAmPm = "";
        if (ampm == calendar.AM) {
            strAmPm = "오전";
        } else if (ampm == calendar.PM) {
            strAmPm = "오후";
        } else {
            // 예외
        }
        if (strAmPm.equalsIgnoreCase("오후")) {
            // 현재 시간이 오후이면 동작 하면 안된다.

            setMidnightAlarm(context);
//            //////////Global_UTIL.file_log( "자정 알림이 오후로 들어왔다 : " + hour);
            return;
        }


        /** 현재카운트 저장 */
        String nowCnt = Preferences.getStepCount(context);
        /** 자정전 날짜를 저장한다. "예)20191101 이면 -1일 제외하여 20191031" */
        String oneMinusDay = getCustomDate();
        Preferences.setMidnightStepInfo(context, nowCnt, oneMinusDay);
        ////////////Global_UTIL.file_log( "DeviceDateEventReceiver onReceive 이벤트 발생 전날 스탭 값 저장 nowCnt : " + nowCnt + " oneMinusDay : " + oneMinusDay);


//        Log.d(TAG, "DeviceDateEventReceiver onReceive 이벤트 발생 페도미터 걸음 변수 초기화 Clear Steps==============================================================");
        // 페도미터 걸음 변수 초기화 Clear Steps
        Preferences.clearStepCount(context);


        /**
         * (1) 자정이 되면 MainActivity 연결된 스마트 밴드 정보를 다시 갱신하기 위해서 호출
         * (2) 걸음수 0으로 초기화 시키며, 홈 걸음수 와 STEP 서비스 노티 걸음수도 0으로 update
         * */
        Intent readyIntent = new Intent("com.ilevit.alwayz.android.midnight");
        context.sendBroadcast(readyIntent);

        // 걷기 홈과 노티에  현재 걸음 수 전송하여 보여지는 화면 업데이트
        Intent midnightIntent = new Intent("stepDataSend");
        midnightIntent.putExtra("STEP_CNT", 0);
        LocalBroadcastManager.getInstance(context).sendBroadcast(midnightIntent);


        /**
         * (1) 자정이 되면 서버에 폴링해서 SEND해서 걸음수 저장 값 초기화
         */
        Preferences.setSendStepCount(context, 0);

        /**
         * 로그아웃한 사용자는 자정이 되었을때 초기화 하지 못한다. 문제는 로그아웃하고 자정이 지나서 로그인하면 해당걸음수를 그대로 가져오게 된다.
         * 그래서 MainActivity를 실행하게 되면 자정알람이 등록이 되는데, 이 날짜보다 전날이면 자정초기화를 하지 않은 사용자라고 판단하고 걸음수를 초기화 한다.
         * 로직은 : 1. 이 클래스에서는 알람이 등록하게 되면 공유객체에 알람 지정 날짜를 저장하게 되는데 여기에서는 현재 날짜를 업데이트 하는 용도로 사용한다.
         *
         * *************사용하지 않음 : intro에서 걸음수가 0이면 초기화 시키는 것으로 변경
         */


        /**
         * 현재 수신된 알람을 취소하고, 다시 알람을 요청한다.
         */
//        if (MainActivity.getInstance() != null) {
//            MainActivity.getInstance().setMidnightAlarm();
//        } else {
//            setMidnightAlarm(context);
//        }
        setMidnightAlarm(context);


        // 서버에 0 걸음수를 전송한다.
        midnightExecute();


        Global_UTIL.file_log(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$자정 DeviceDateEventReceiver onReceive 이벤트 발생$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ END");
    }


    /**
     * 걸음수 0 초기화 하고 걸음수 0 서버에 전송
     */
    private void midnightExecute() {


        if (mContext == null) {
            return;
        }

        //////////Global_UTIL.file_log( "==========================================Firebase midnightExecute start");

        /** 현재카운트 저장 */
        String appToken = Preferences.get_app_token(mContext);//토큰값


        /** 일반 걸음수 전송 */
        StepDataModel sdm = new StepDataModel();
//        sdm.setStepCnt("0");
//        sdm.setBandConnNm(bandConnNm);
//        sdm.setSetpCreateDtime(getTime);

        // 현재 전송한 값을 공유 객체에 임시 저장한다.
        Preferences.setSendStepCount(mContext, Integer.valueOf("0"));

        // 서버에 전송한다.
        retrofitClient.create(StepService.class).setSteps_post(appToken, sdm).enqueue(setSteps_post);


    }


    /**
     * 현재 자기 걸음수 전송
     */
    RetrofitCallBack<ResponseBody> setSteps_post = new RetrofitCallBack<ResponseBody>() {
        public void onSuccess(ResponseBody responseValue, HttpUrl url, Response<ResponseBody> response) {


            ////////////Global_UTIL.file_log( "서버에 전송하는 setSteps_post");

            int result_cd = response.code();

            if (result_cd == 200) {
                // 정상처리


                // 현재 걸음수 서버에 전송 날짜 저장
                String nowDate = Global_UTIL.getNowDate();
                Preferences.setSendDate(mContext, nowDate);



//                Log.d(TAG, "**** 걸음수 서버에 전송 완료");
            }

        }


        @Override
        public void onNetworkFail(int code, String message) {

//            Global_UTIL.toastMessageShow(mContext, message, 100);


        }

        @Override
        public void onFail(int code, String message, Response<ResponseBody> extraObject) {
            int http_code = code;
            String msg = message;

//            Global_UTIL.toastMessageShow(mContext, "걸음 전송에 실패 하였습니다.\n" + message, 200);

        }

    };


    /**
     * 서버 결과 Code 값에 따라 에러 처리
     */
    private boolean responseResultChk(String value, HttpUrl url) {
        if (value.equalsIgnoreCase("N")) {
            return false;
        } else if (value.equalsIgnoreCase("E")) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 자정 알림 알람 설정
     */
    public void setMidnightAlarm(Context context) {

        // 알람 설정 확인
        Intent intent = new Intent(context, DeviceDateEventReceiver.class);
        // PendingIntent.FLAG_NO_CREATE 플래그를 사용하여 PendingIntent.getActivity, PendingIntent.getBroadcast, PendingIntent.getService 등을 호출해 보면 된다. 이미 설정된 알람이 없다면 null 을 반환한다.
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        // result == true 이면 설정되지 않았음
        boolean result = (pIntent == null);

        // 자정 알람 설정
        if (result == true) {
            Log.d(TAG, "onCreate 자정 알림 알람 설정 ON");
            Global_UTIL.resetAlarm(context);
        } else {
            Log.d(TAG, "onCreate 자정 알림 알람 설정 OFF");


            String nowDate = Global_UTIL.getNowDate();
            String alarmDate = Preferences.getAlarmDate(context);

            if (nowDate.equalsIgnoreCase(alarmDate)) {

                // TODO : 이미 지난 알림이라면 취소하고(알람이 오늘 날짜이면 취소)
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent cancelIntent = new Intent(context, DeviceDateEventReceiver.class);
                PendingIntent sender = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (sender != null) {
                    am.cancel(sender);
                    sender.cancel();

                    // 취소하고 다시 알람을 맞추기 위해서 다시 호출
                    setMidnightAlarm(context);
                }
            }//end if
        }

    }


    /**
     * 현재날짜에서 1일전 날짜 가져오기
     *
     * @return
     */
    private String getCustomDate() {

        Calendar c1 = new GregorianCalendar();
        c1.add(Calendar.DATE, -1); // 오늘날짜로부터 -1
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); // 날짜 포맷
        String d = sdf.format(c1.getTime()); // String으로 저장
        return d;
    }


}
