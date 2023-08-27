package com.ilevit.alwayz.android.util;

import android.content.Context;
import android.content.SharedPreferences;


public class Preferences {



    /**
     * 단말기 걸음 센서 유무 확인
     */
    public static void setSensorYN(Context context, boolean gpsYN) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean("stepYN", gpsYN);
        prefsEditor.apply();
    }

    public static boolean getSensorYN(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("stepYN", false);
    }



    /**
     * ============================================= 앱 동작 관련 SHARED_PREFERENCE START =============================================
     */


    public static String get_app_token(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("app_token", "");
    }

    public static void set_app_token(Context context, String token) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("app_token", token);
        prefsEditor.apply();
    }

    // ============================================= 앱 동작 관련 SHARED_PREFERENCE END =============================================



    /**
     * ============================================= 자정 걷기 전송 확인 SHARED_PREFERENCE START =============================================
     */

    // 자정이 되었을때 전송되니, 마지막 전송일을 알 수가 있다.
    public static void setMidnightStepInfo(Context context, String sendStepCnt, String sendDtime) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("sendStepCnt", sendStepCnt);
        prefsEditor.putString("sendDtime", sendDtime);
        prefsEditor.apply();
    }

    public static String getMidnightSendStepCnt(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("sendStepCnt", "0");
    }

    public static String getMidnightSendDtime(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("sendDtime", "");
    }


    // ============================================= 자정 걷기 전송 확인 SHARED_PREFERENCE START =============================================


    /**
     * ============================================= 걷기 관련 SHARED_PREFERENCE START =============================================
     */

    // 서버에 전송완료한 걸음수 날짜
    public static void setSendDate(Context context, String sendDate) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("sendDate", sendDate);
        prefsEditor.apply();
    }


    // 서버에 전송완료한 걸음수 날짜
    public static String getSendDate(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("sendDate", "");
    }


    public static boolean getStepWho(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("setpWho", false);
    }

    /**
     * 재부팅 알림 스탭 계산 처리 flag
     *
     * @param context
     * @param whoStep
     */
    // Set how many steps I have walked.
    public static void setStepWho(Context context, boolean whoStep) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean("setpWho", whoStep);
        prefsEditor.apply();
    }

    public static String getStepCount(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return String.format("%d", prefs.getInt("stepCount", 0));
    }

    /**
     * 재부팅 및 걸음 수 계산을 하기 위해서 저장
     *
     * @param context
     * @param steps
     */
    public static void setStepCountSubtract(Context context, Integer steps) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("stepCountSubtract", steps);
        prefsEditor.apply();
    }

    public static String getStepCountSubtract(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return String.format("%d", prefs.getInt("stepCountSubtract", 0));
    }


    /**
     * 현재 사용자에게 표시할 걸음
     *
     * @param context
     * @param steps
     */
    // Set how many steps I have walked.
    public static void setStepCount(Context context, Integer steps) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("stepCount", steps);
        prefsEditor.apply();
    }


    public static void setSendStepCount(Context context, Integer steps) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("sendStepCount", steps);
        prefsEditor.apply();
    }



    public static String getSendStepCount(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return String.format("%d", prefs.getInt("sendStepCount", 0));
    }


    // Reset the Subtract Step Count (On Boot)
    public static void clearStepCount(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("stepCountSubtract", 0);
        prefsEditor.putInt("stepCount", 0);
        prefsEditor.apply();
    }
    // ============================================= 걷기 관련 SHARED_PREFERENCE END =============================================


    /**
     * ============================================= 밴드 연동 flag 관련 SHARED_PREFERENCE START =============================================
     * <p>
     * app : 1, samsung fit : 2, fitbit : 3, polar : 4, misfit : 5, garmin  : 6, patron : 7 ,inbody : 8
     */
    public static void setStepConnWhat(Context context, String what) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("step_type", what);
        prefsEditor.apply();
    }

    public static String getStepConnWhat(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("step_type", "1");
    }

    public static void setStepDisConnectionLastData(Context context, int last_step) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("last_step", last_step);
        prefsEditor.apply();
    }

    public static String getStepDisConnectionLastData(Context context) {
        SharedPreferences prefs = Global.gContext.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return String.format("%d", prefs.getInt("last_step", 0));
    }

    /**
     * ============================================= 알람 날짜 데이터 저장 SHARED_PREFERENCE START =============================================
     */
    public static void setAlarmDate(Context context, String setAlarmDate) {
        SharedPreferences prefs = context.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("setAlarmDate", setAlarmDate);
        prefsEditor.apply();
    }

    public static String getAlarmDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Global.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString("setAlarmDate", "");
    }


    // ============================================= 알람 날짜 데이터 저장 SHARED_PREFERENCE END =============================================

}