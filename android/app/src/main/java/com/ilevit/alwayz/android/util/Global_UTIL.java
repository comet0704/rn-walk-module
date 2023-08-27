package com.ilevit.alwayz.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ilevit.alwayz.android.R;
import com.ilevit.alwayz.android.receiver.DeviceDateEventReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Global_UTIL {



    /**
     * 파일 로그 생성
     *
     * @param TAG
     * @param TEXT
     */
    public static void file_log(String TAG, String TEXT) {
        try {
            LogWrapper.v(TAG, TEXT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 자정을 알려주는 알람
     *
     * @param context
     */
    public static void resetAlarm(Context context) {
        AlarmManager resetAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, DeviceDateEventReceiver.class);
        resetIntent.putExtra("DATE_CHAGE", "Y");
        PendingIntent resetSender = PendingIntent.getBroadcast(context, 0, resetIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);


        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0); // 현재 시간(24시간제)
        resetCal.set(Calendar.MINUTE, 0); // 현재 분
        resetCal.set(Calendar.SECOND, 0); // 현재 초
//        resetCal.set(Calendar.HOUR_OF_DAY, 13);
//        resetCal.set(Calendar.MINUTE, 27);
//        resetCal.set(Calendar.SECOND, 0);


        /**
         * setInexactRepeating() 함수는 이름이 의미하는 것처럼 정확도가 떨어지는 알람 설정 함수입니다. 오차를 허용하는 알람을 설정할 때 사용한다고 이해하시면 될 것 같고요.
         * API 19 미만에서는 setRepeating() 함수를 이용해서 정확한 반복 알람을 설정할 수 있었지만 API 19부터는 이 함수 조차도 정확한 시간을 보장하지 않습니다.
         * 따라서 정확한 알람을 반복하기 위해서는 setExact() 함수를 이용해야 하고, 반복(다음 알람 트리거 시간)에 대한 부분은 직접 구현을 해야 합니다
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 23 이상인 OS
            resetAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, resetSender);
//            resetAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis(), resetSender); // 테스트
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 19 이거나 이하일 경우
            resetAlarmManager.setExact(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, resetSender);
        } else {
            // 둘중도 아니라면
//            resetAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender); // 오늘날짜에 1일 플러스
            resetAlarmManager.set(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, resetSender);
        }


        // 다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
//        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender); // 오늘날짜에 1일 플러스
//        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, resetSender); // 테스트
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setResetTime = format.format(new Date(resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY));

        // 알람 시간을 계산을 하기 위해서 공유 변수에 저장
//        SimpleDateFormat format2 = new SimpleDateFormat("MM/dd kk:mm:ss");
//        String setResetTime2 = format2.format(new Date(resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY));
//        Preferences.setAlarmDate(context, setResetTime2);


        Log.d("resetAlarm", "ResetHour : " + setResetTime);
//        Log.d("resetAlarm", "ResetHour : " + setResetTime2);
    }


    public static String stringToDateDayDot(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy.MM.dd");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    public static String stringToDateDay(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy.MM.dd");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    /**
     * 생성 날짜 20191120235959 ==> 2019년11월20일 오전오후 시간:분 형태로 변경   #날짜
     *
     * @param str_date
     * @return
     */
    public static String stringToDate(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년MM월dd일 a hh:mm");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    public static String stringToDate2(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    public static String stringToDate3(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    public static String stringToDate4(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }

    public static String stringToDate5(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }


    public static String getNowDateDetail() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일 a hh:mm");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }


    public static String getNowDateDetail2(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년 MM월 dd일");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }


    /**
     * 오늘 날짜 구함
     *
     * @return
     */
    public static String getNowDate() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }


    public static String getNowDate2() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }


    public static String getNowDateKor() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }


    public static String getNowDateTimeTest() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일 a hh:mm:ss");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }

    public static String getNowDateTime() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일 a hh:mm");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }

    public static String getNowDateTime2() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }

    public static String getNowDateTime3() {

        // 오늘 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("MM월dd일 a hh:mm");
//        String getTime = sdf.format(date);


        return sdf.format(date);

    }

    //현재 날짜 주차

    public static String getWeek() {

        Calendar c = Calendar.getInstance();

        String week = String.valueOf(c.get(Calendar.WEEK_OF_MONTH));

        return week;

    }

    //특정 년,월,주 차에 월요일 구하기

    public static String getMonday(String yyyy, String mm, String wk) {

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM월 dd일");

        Calendar c = Calendar.getInstance();


        int y = Integer.parseInt(yyyy);

        int m = Integer.parseInt(mm) - 1;

        int w = Integer.parseInt(wk);


        c.set(Calendar.YEAR, y);

        c.set(Calendar.MONTH, m);

        c.set(Calendar.WEEK_OF_MONTH, w);

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return formatter.format(c.getTime());

    }






    /**
     * 오늘 년도 구함
     *
     * @return
     */
//    public static String getNowYear() {
//
//        // 오늘 날짜 구함
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
////        String getTime = sdf.format(date);
//
//
//        return sdf.format(date);
//
//    }
    public static String stringToDateYear(String str_date) {

        String send_date = "";

        String strDate = str_date;
        String createDate = "";
        // DATE 형태로 변경
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");

            Date date = sdf.parse(strDate);
            send_date = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            send_date = str_date;
        }
        return send_date;
    }



}

