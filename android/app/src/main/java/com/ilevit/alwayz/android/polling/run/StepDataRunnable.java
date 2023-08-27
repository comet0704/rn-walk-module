package com.ilevit.alwayz.android.polling.run;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ilevit.alwayz.android.MainActivity;
import com.ilevit.alwayz.android.net.retrofit.RetrofitCallBack;
import com.ilevit.alwayz.android.net.retrofit.RetrofitClient;
import com.ilevit.alwayz.android.net.retrofit.model.StepDataModel;
import com.ilevit.alwayz.android.net.retrofit.service.StepService;
import com.ilevit.alwayz.android.receiver.DeviceDateEventReceiver;
import com.ilevit.alwayz.android.util.Global;
import com.ilevit.alwayz.android.util.Global_UTIL;
import com.ilevit.alwayz.android.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StepDataRunnable implements Runnable {


    private static final String TAG = StepDataRunnable.class.getSimpleName();

    private Retrofit retrofitClient = null;

    private Context mContext = null;

    private boolean mEventMidnightCheck = false;

    public StepDataRunnable(Context context) {
        super();

        mContext = Global.gContext;
    }

    public void run() {
        // 오늘 날짜 구함s
        String testNowCnt = Preferences.getStepCount(mContext); // 현재 걸음수
        String getNowDateTime = Global_UTIL.getNowDateTimeTest();
        Global_UTIL.file_log(TAG, "****************************걸음수 : StepDataRunnable 20초 동안 계속 실행 현재 걸음수 : " + testNowCnt + " 스케줄러 실행 시간 : " + getNowDateTime);


        /**
         * 자정알림이 안되는 단말기를 위한 로직
         * 1.  setCheckMidnight() : 자정체크는 현재 시간이 00 시일경우 걸음수가 500 걸음 이상일 경우에 초기화 로직을 실행하도록 한다.
         * 2.  setCheckDay()      : 전날짜에서 현재 날짜와 다르면 초기화 로직을 실행한다.
         */
        setCheckMidnight();
        setCheckDay();



        if (retrofitClient == null) {

            Global_UTIL.file_log(TAG, "retrofitClient null");

            retrofitClient = RetrofitClient.getInstance().getRetrofit();
        }


        /** 매일 자정 알림 체크 */
        setMidnightAlarm();


        // 오늘 날짜 구함s
        String getTime = Global_UTIL.getNowDate();


        // 공유 객체에서 현재 스탭 정보 가져오기
        String stepCnt = Preferences.getStepCount(mContext); // 현재 걸음수
        String bandConnNm = Preferences.getStepConnWhat(mContext); // 스마트 밴드 연동 타입 app : 1, samsung fit : 2, fitbit : 3, polar : 4, misfit : 5, patron : 6, garmin : 7, inbody : 8, google : 9
        String stepUpdateDtime = getTime;// 오늘날짜
        String appToken = Preferences.get_app_token(mContext);//토큰값


        ////////////Global_UTIL.file_log( "서버에 전송하는 걸음수 확인  : 걸음수 ==> " + stepCnt);


        /** 자정 걸음수 전송 */
        String midnightCnt = Preferences.getMidnightSendStepCnt(mContext);
        String midnightDtime = Preferences.getMidnightSendDtime(mContext);
        if (!midnightDtime.equalsIgnoreCase("")) {

            StepDataModel sdm = new StepDataModel();
//            sdm.setStepCnt(midnightCnt);
//            sdm.setBandConnNm(bandConnNm);
//            sdm.setSetpCreateDtime(midnightDtime);

            ////////////Global_UTIL.file_log( "전날 걸음수 전송  : 걸음수 ==> " + midnightCnt +    "  밴드연결 ==> " +  bandConnNm  +  " 업데이트 날짜 : " + midnightDtime);


            // 자정 전송 값 초기화
            Preferences.setMidnightStepInfo(mContext, "", "");

//            // 서버에 전송한다.
//            retrofitClient.create(StepService.class).setSteps_post(appToken, sdm).enqueue(setStepsMidNight_post);
        }

        /** 이벤트 걸음 전송을 위한 계산 */
        int minusStep = Integer.valueOf(Preferences.getSendStepCount(mContext));// 서버에 전송한 걸음수
        int nowCnt = Integer.valueOf(Preferences.getStepCount(mContext)); // 현재 걸음수

        /** 현재 걸음수가 0이 되면 서버에 전송한 걸음수가 그대로 남아 있어 문제가 발생되어 SEND 전송한 걸음수를 0으로 초기화 한다. */
        if (nowCnt == 0) {
            Preferences.setSendStepCount(mContext, 0);
            minusStep = 0;
        }


        /** 예외 1 : 전송한 걸음수와 동일하면 서버에 전송하지 않는다. */
        if (minusStep != 0) {
            // 자정으로 sendStepData 0으로 초기화때에는 예외 제외
            String sendStepCnt = String.valueOf(minusStep);
            if (stepCnt.equalsIgnoreCase(sendStepCnt)) {
                return;
            }
        }

        /** 예외 2 : 현재 걸음수가 0이면 전송하지 않는다. */
        if (nowCnt == 0) {
            return;
        }

        /** 음수 값이면 리턴 **/
        if (nowCnt < 0) {

            return;
        }


        /** 일반 걸음수 전송 */
        StepDataModel sdm = new StepDataModel();
//        sdm.setStepCnt(stepCnt);
//        sdm.setBandConnNm(bandConnNm);
//        sdm.setSetpCreateDtime(stepUpdateDtime);

        // 현재 전송한 값을 공유 객체에 임시 저장한다.
        Preferences.setSendStepCount(mContext, Integer.valueOf(stepCnt));

        ////////////Global_UTIL.file_log( "서버에 전송하는 걸음수 VO  ==> " + sdm.toString());

        // 서버에 전송한다.
//        retrofitClient.create(StepService.class).setSteps_post(appToken, sdm).enqueue(setSteps_post);


    }


    /**
     * 자정전송
     */
    RetrofitCallBack<ResponseBody> setStepsMidNight_post = new RetrofitCallBack<ResponseBody>() {
        public void onSuccess(ResponseBody responseValue, HttpUrl url, Response<ResponseBody> response) {

            ////////////Global_UTIL.file_log( "서버에 전송하는 setStepsMidNight_post");


            int result_cd = response.code();

            if (result_cd == 200) {
                // 정상처리
//                Log.d(TAG, "**** 자정 걸음수 서버에 전송 완료");

                // TODO : 성공하게 되면 공유객체를 생성하여 현재 부터 걸음수를 저장하게 한다.
                // 자정 전송 값 초기화
                Preferences.setMidnightStepInfo(mContext, "", "");


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
                Global_UTIL.file_log(TAG, "**** 걸음수 서버에 전송 완료");
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
     * 자정알림이 안되는 단말기를 위한 로직
     * 1. 자정체크는 현재 시간이 00 시일경우 걸음수가 1000 걸음 이상일 경우에 초기화 로직을 실행하도록 한다.
     */
    private void setCheckMidnight() {

        // [날짜 비교하기]
//        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        Date date1 = null;
        Date date2 = null;
        try {
            // 현재 시간
            // 오늘 날짜 구함
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            String StrDate = sdf.format(date);
            date1 = sdf.parse(StrDate);

//            //////////Global_UTIL.file_log( "현재 시간 : " + StrDate);


            // 비교할 시간
            String diffDate = Global_UTIL.getNowDate() + "0001";
            date2 = sdf.parse(diffDate);

//            //////////Global_UTIL.file_log( "자정에서 1분 시간 : " + diffDate);

            //date1이 date2보다 이후 일때 true, 아니면 false
            boolean checkDay = date1.after(date2);


            // 비교
            if (checkDay == false) {
                // 현재날짜의 시간의 분이 00:01 분이 안지났을때

                // TODO : 현재 카운트 체크 후 500 이상이면 초기화 로직 실행함

                // 현재 걸음수 가져오기
                int nowCnt = Integer.valueOf(Preferences.getStepCount(mContext)); // 현재 걸음수

//                //////////Global_UTIL.file_log( "check day : " + checkDay + "     현재 걸음수 : " + nowCnt);

                if (nowCnt > 0) {
                    midnightExecute();
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 전날짜에서 현재 날짜와 다르면 초기화 로직을 실행한다.
     */
    private void setCheckDay() {

        /** 예외1  현재 걸음수가 0이면 처리 하지 않기 **/
        String stepCnt = Preferences.getStepCount(mContext); // 현재 걸음수
        if (stepCnt == "0") {
            return;
        }

        // 오늘 날짜 구함
        String nowDate = Global_UTIL.getNowDate();

        // 서버에 전송한 날짜 구함
        String preDate = Preferences.getSendDate(mContext);

        if (preDate.equalsIgnoreCase("")) {
            // 전송한 이력이 없으면 처리하지 않도록 한다.
            return;
        }

        /** 오늘 날짜와 걸음 전송한 날짜가 다르면 걸음수 초기화가 안된것으로 파악하여 걸음수 초기화 안된것으로 파악하고 , 서버에 걸음수 0 전송 */
        if (!nowDate.equalsIgnoreCase(preDate)) {
            // 날짜가 틀리면 걸음수 초기화 하고, 서버에 걸음수 전송한다.

            midnightExecute();

            ////////////Global_UTIL.file_log( "전날짜에서 현재 날짜와 다르면 초기화 로직을 실행한다. setCheckDay");

        }


    }

    /**
     * 걸음수 0 초기화 하고 걸음수 0 서버에 전송
     */
    private void midnightExecute() {


        if (mContext == null) {
            return;
        }


        /** 현재카운트 저장 */
//        String nowCnt = Preferences.getStepCount(mContext); // 현재 걸음수
        String appToken = Preferences.get_app_token(mContext);//토큰값
        String bandConnNm = Preferences.getStepConnWhat(mContext); // 스마트 밴드 연동 타입 app : 1, samsung fit : 2, fitbit : 3, polar : 4, misfit : 5, patron : 6, garmin : 7, inbody : 8, google : 9
        String getTime = Global_UTIL.getNowDate();// 오늘 날짜 구함s

        // 페도미터 걸음 변수 초기화 Clear Steps
        Preferences.clearStepCount(mContext);


        /**
         * (1) 자정이 되면 MainActivity 연결된 스마트 밴드 정보를 다시 갱신하기 위해서 호출
         * (2) 걸음수 0으로 초기화 시키며, 홈 걸음수 와 STEP 서비스 노티 걸음수도 0으로 update
         * */
        Intent readyIntent = new Intent("com.ilevit.alwayz.android.midnight");
        mContext.sendBroadcast(readyIntent);

        // 걷기 홈과 노티에  현재 걸음 수 전송하여 보여지는 화면 업데이트
        Intent midnightIntent = new Intent("stepDataSend");
        midnightIntent.putExtra("STEP_CNT", 0);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(midnightIntent);


        /**
         * (1) 자정이 되면 서버에 폴링해서 SEND해서 걸음수 저장 값 초기화
         */
        Preferences.setSendStepCount(mContext, 0);


        /** 일반 걸음수 전송 */
        StepDataModel sdm = new StepDataModel();
//        sdm.setStepCnt("0");
//        sdm.setBandConnNm(bandConnNm);
//        sdm.setSetpCreateDtime(getTime);

        // 현재 전송한 값을 공유 객체에 임시 저장한다.
        Preferences.setSendStepCount(mContext, Integer.valueOf("0"));

        // 서버에 전송한다.
        retrofitClient.create(StepService.class).setSteps_post(appToken, sdm).enqueue(setSteps_post);


        ////////////Global_UTIL.file_log( "*************************************************자정  걸음수 0 midnightExecute 걸음수 초기화 했다.");


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


    /**
     * 자정 알림 알람 설정
     */
    public void setMidnightAlarm() {

        // 알람 설정 확인
        Intent intent = new Intent(mContext, DeviceDateEventReceiver.class);
        // PendingIntent.FLAG_NO_CREATE 플래그를 사용하여 PendingIntent.getActivity, PendingIntent.getBroadcast, PendingIntent.getService 등을 호출해 보면 된다. 이미 설정된 알람이 없다면 null 을 반환한다.
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_NO_CREATE);
        // result == true 이면 설정되지 않았음
        boolean result = (pIntent == null);

        // 자정 알람 설정
        if (result == true) {
            Log.d(TAG, "onCreate 자정 알림 알람 설정 ON");
            Global_UTIL.resetAlarm(mContext);
        } else {
            Log.d(TAG, "onCreate 자정 알림 알람 설정 OFF");


            String nowDate = Global_UTIL.getNowDate();
            String alarmDate = Preferences.getAlarmDate(mContext);

            if (nowDate.equalsIgnoreCase(alarmDate)) {

                // TODO : 이미 지난 알림이라면 취소하고(알람이 오늘 날짜이면 취소)
                AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                Intent cancelIntent = new Intent(mContext, DeviceDateEventReceiver.class);
                PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (sender != null) {
                    am.cancel(sender);
                    sender.cancel();

                    // 취소하고 다시 알람을 맞추기 위해서 다시 호출
                    setMidnightAlarm();
                }
            }//end if
        }

    }


}