package com.ilevit.alwayz.android.service;


import static com.ilevit.alwayz.android.util.Global.gContext;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat.Builder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ilevit.alwayz.android.AlarmReceiver;
import com.ilevit.alwayz.android.MainActivity;
import com.ilevit.alwayz.android.R;
import com.ilevit.alwayz.android.polling.PollingManager;
import com.ilevit.alwayz.android.polling.ScheduledExecutor;
import com.ilevit.alwayz.android.util.Global;
import com.ilevit.alwayz.android.util.Global_UTIL;
import com.ilevit.alwayz.android.util.Preferences;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


/**
 * 시작타입 서비스 (started service)
 * onStartCommand
 * - 서비스가 호출될 때마다 실행
 * - 동작 모드 (return 값)
 * START_STICKY : 시스템에 의해 종료되어도 리소스가 여유로워지면 다시 서비스 시작
 * START_NOT_STICKY : 시스템에 의해 종료되어도 다시 서비스 안 됨
 * START_REDELIVER_INTENT : START_STICKY 와 비슷하며, Intent 값까지 모두 요주시켜 줌
 * <p>
 * **************Service 수명주기에 따라 콜백되는 메서드**************
 * onCreate         : service가 생성된 뒤에 콜백된다.
 * onStartCommand   : service가 시작된 뒤에 콜백된다.
 * onBind           : Context.bindService()를 통해 이 service가 바인드되는 경우 호출된다. 또한 바인드 후, 서비스에 접속할 때는 ServiceConnection,onServiceConnected가 콜백된다.
 * onRebind         : 이 service가 언바인드된 다음, 다음 접속했을 때 콜백된다.
 * onUnbind         : 이 service가 언바인드될 때 콜백된다.
 * onDestroy        : service가 폐기되기 직전에 콜백된다.
 */
public class NotificationStepService extends Service implements SensorEventListener {

    private static final String TAG = NotificationStepService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 8301;
    public static final String CHANNEL_ID = "AlwaysStepServiceChannel";

    private Builder mBuilder;
    private NotificationManager mNotificationManager;
    private Context mContext = null;
    private SensorManager sensorManager = null;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Global_UTIL.file_log(TAG, "==========================================onStartCommand 실행");

        mContext = gContext;

        // 걸음 센서 매니져 초기화
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

        // 걷기 데이터를 전송하는 스케줄러 등록
        stepPollingStart();
       

        // Get Notification Manager
        mBuilder = new Builder(this, CHANNEL_ID);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 채널 생성
        createNotificationChannel();


        /** 걸음 센서 설정 */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 누적되는 걸음 센서
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 가속도 센서
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (countSensor != null) {

            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            // 걸음 센서 있음
            Preferences.setSensorYN(mContext, true);
        } else if (accelerometer != null) {

            // TODO : 가속도 센서를 이용하여 걸음을 작동시킨다 : 20년 2월 13일
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            // 걸음 센서 없음
            Preferences.setSensorYN(mContext, false);
        }


        /** Setup First Notification */
        updateNotification(true);

        // 서비스에서 수신받는 브로드캐스트 레지스터 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("stepDataSend"));

        ////////////Global_UTIL.file_log( "START_STICKY : " + mContext.toString());

        // Restart the service if its killed
        return START_STICKY;

    }

    private void stepPollingStart() {

        Global_UTIL.file_log(TAG, "==========================================stepPollingStart 실행");

        PollingManager._scheduled_executor = new ScheduledExecutor(1);

        new Thread(new Runnable() {
            @Override
            public void run() {

                ////////////Global_UTIL.file_log( "걸음수 폴링 시작 stepPollingStart  run : " + mContext.toString());
                PollingManager.pollingStepData(Global.gContext);// 걷기 데이터 전송
            }
        }).start();
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createNotificationChannel() {

        Global_UTIL.file_log(TAG, "==========================================createNotificationChannel 실행");

//        Log.d(TAG, "createNotificationChannel 실행");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 오레오 버전부터 채널 사용

            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "걸음", NotificationManager.IMPORTANCE_LOW);
            serviceChannel.setShowBadge(false);

            mNotificationManager.createNotificationChannel(serviceChannel);
        }
    }


    public void updateNotification(boolean firstTime) {


        Global_UTIL.file_log(TAG, "==========================================updateNotification 실행");


        String appBandText = "올웨이즈";
        mBuilder.setContentTitle(Preferences.getStepCount(gContext) + " 걸음" + " " + appBandText);


        Intent notificationIntent = new Intent(gContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(gContext, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification;

        // First Time Setup
        if (firstTime) {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
//            mBuilder.setPriority(Notification.PR);
            mBuilder.setContentText("");

            mBuilder.setOngoing(true); // 알림창을 옆으로 밀어도 지워지지 않게 하는 옵션
            mBuilder.setAutoCancel(false); // 알림창을 클릭하면 자동으로 지워지도록 하는 옵션
            mBuilder.setOnlyAlertOnce(true);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setNumber(0); // 확인하지 않은 알림의 개수를 표시하는 옵션
            notification = mBuilder.build();

            // Set Service to run in the Foreground
            startForeground(NOTIFICATION_ID, notification);
        } else {

            // 뱃지 초기화
            mBuilder.setNumber(0); // 확인하지 않은 알림의 개수를 표시하는 옵션
            mBuilder.setContentText("");

            notification = mBuilder.build();
        }

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.e(TAG, "onSensorChanged 이벤트 실행");

        /**
         *  스마트 밴드 및 삼성헬스 연동 전송 실행
         */
        // 연결 / 비연결 체크  app : 1, samsung fit : 2, fitbit : 3, polar : 4, misfit : 5, garmin  : 6, patron : 7 ,inbody : 8
        String stepConnWhat = Preferences.getStepConnWhat(Global.gContext);

        /**
         * TODO : 가속도 센서 걸음 수 측정
         */
        boolean senserYN = Preferences.getSensorYN(Global.gContext);
        if (senserYN == false) {
            // 가속도 걸음 측정 실행
            accelerometerStepChanged(event);
        }

        /** 예외처리 : 스탭값이 - 일경우에 stap_cal 값을 0으로 변경한다. */
        String nowStepCnt = Preferences.getStepCount(Global.gContext);
        if (Integer.valueOf(nowStepCnt) < 0) {
//            Log.d(TAG, "예외처리 : 스탭값이 - 일경우에 stap_cal 값을 0으로 변경한다. : " + nowStepCnt);
            // 음수
            Preferences.setStepCountSubtract(Global.gContext, 0);
            Log.e(TAG, "onSensorChanged 이벤트 실행 13");
        }


        // 단말기 스탭 카운터 센서 걸음수 값 update Step Counter
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            // 센서 걸음 수 값
            int eventValues = (int) event.values[0];
//            Log.d(TAG, "onSensorChanged : " + (int) event.values[0]);


            /** TYPE_STEP_COUNTER 값은 단말기 재부팅 시에만 0으로 초기화 된다. 그외는 0으로 안된다. */

            // 공유객체 에서 계산해야될 값 stepCountSubtract 불러오기
            String getStepCountSubtract = Preferences.getStepCountSubtract(this);  // 재부팅 및 걸음 수 계산을 하기 위해서
            int step_cal = Integer.valueOf(getStepCountSubtract);


            /** 예외처리 1 : 공유객체에 저장된 스탭값이 0이면 걸음 값을 저장한다.(자정에 step_cnt, step_cal 값을 0으로 초기화 하기 때문에) */
            // ***단말기 재부팅 하였을경우 TYPE_STEP_COUNTER 값이 0
            if (eventValues == 0) {
                step_cal = Integer.valueOf(Preferences.getStepCount(this));
                // 재부팅 로직만 타게함
                Preferences.setStepWho(mContext, true);
            }


            /**
             * - step_cal이 0일 경우에는 최초 실행하거나, 앱을 삭제 후 실행했을 경우
             * - 밴드 연동하여 사용했다가, 자체페도미터로 돌아왔을때 getStepCountSubtract "0"이 된다
             */
            if (step_cal == 0) {

                step_cal = eventValues;

                // TODO : 스마트 밴드에서 기본 페도미터로 변경되었을 경우, 스마트 밴드의 걸음수를 현재 걸음으로 적용 하기 위함
                String getStepDisConnectionLastData = Preferences.getStepDisConnectionLastData(this);
                // 스마트밴드의 마지막 값이 0이 아닐경우에는 스마트 밴드 걸음수 값을 step_cal에 적용한다.
                if (!getStepDisConnectionLastData.equalsIgnoreCase("0")) {
//                    Log.d(TAG, "getStepDisConnectionLastData step_cal 변경전 : " + step_cal);
                    step_cal = step_cal - Integer.valueOf(getStepDisConnectionLastData);
//                    Log.d(TAG, "getStepDisConnectionLastData step_cal 변경후: " + step_cal);
                    // 스마트 마지막 걸음수 값은 0으로 초기화 한다.
                    Preferences.setStepDisConnectionLastData(this, 0);
                }

                // 일반 로직만 타게함
                Preferences.setStepWho(mContext, false);
            }


            // TODO : 전날에 STEP_CAL 값이 작았는데, 그 값이 어느세 EVENT 값보다 작아질때 일반 STEP으로 계산되어서 초기화 되어 다시 올라가는현상(핸드폰을 12시에 재부팅 했거나, 그때 앱이 완전 실행이 안됐을 경우)


            /**
             * 로그아웃 하고, 다시 로그인하고 나서 서버에서 내 현재 데이터를 받았을 경우
             */
            if (Global.MEM_LOGOUT_CHECK == true) {

                // 한번만 실행되도록 한다.
                Global.MEM_LOGOUT_CHECK = false;

                // 서버에서 보내준 걸음수
                int my_stepCnt = Integer.valueOf(Global.MEM_MYTODAYSTEP);

                // 임시 저장된 서버에서 준 걸음수 0으로 초기화 한다.
                Global.MEM_MYTODAYSTEP = "0";

                // 이상황일때에는 센서 누적 걸음수값을 계산변수 cal 값을 넣고, 내 현재 걸음을 마이너스 시킨다.
                step_cal = eventValues - my_stepCnt;

                // 일반 로직만 타게함
                Preferences.setStepWho(mContext, false);


                ////////////Global_UTIL.file_log( "재로그인시 걸음수 계산  NOTIFICATIONSTEPSERVICE, 서버에서준 걸음수 : " + my_stepCnt   + "   계산 STEP_CAL : " + step_cal);

            }


            /** *****************************   재부팅 및 걸음 수 계산을 하기 위해서 저장 ********************************************* */

            Log.e(TAG, "onSensorChanged 이벤트 실행 11------" + step_cal);
            Preferences.setStepCountSubtract(this, step_cal);

            /*
             * 예외처리 2 : 단말기 재부팅 되었을 경우 TYPE_STEP_COUNTER 값은 초기화 되었을 경우
             * true : step_cal이 event보다 크면 이것은 재부팅 되어서 초기화 된 값으로 판정 : 이때는 플러스를 적용한다.
             * false : step_cal이 event보다 작으면 : 이때는 event - step_cal을 적용한다.
             */
            int callBackCnt = 0;
            boolean check_step = Preferences.getStepWho(mContext);
            if (check_step == true) {
                //재부팅 로직
                Log.d(TAG, "onSensorChanged " + " STEP_CAL : " + step_cal + " EVENT : " + eventValues + "재부팅 로직 실행 ==> step : " + (eventValues + step_cal));
                callBackCnt = eventValues + step_cal;
            }

            if (check_step == false) {
                //일반 로직
                Log.d(TAG, "onSensorChanged " + " STEP_CAL : " + step_cal + " EVENT : " + eventValues + " ==> 일반 로직 실행 ==> step : " + (eventValues - step_cal));
                callBackCnt = eventValues - step_cal;
            }


            /** 페도미터 걸음수 자체 전송(메인) */
            try {

                Log.e(TAG, "onSensorChanged 이벤트 실행 13");
                // Record Step Count
                Preferences.setStepCount(this, callBackCnt);


                // 걷기 홈에 걸음 수 전송
                Intent intent = new Intent("stepDataSend");
                intent.putExtra("STEP_CNT", callBackCnt);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                // Update Notification Bar
                updateNotification(false);

            } catch (NullPointerException e) {
                e.printStackTrace();

                Global_UTIL.file_log(TAG, "==========================================runnableCode e : " + e.getMessage());
            }


        }//end 단말기 센서 걸음 로직


    }


    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

//        Log.d(TAG, "onAccuracyChanged 실행 ");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Service 클래스에는 Task가 종료되었을 때 콜백을 받는 onTaskRemoved()메서드가 존재한다.
     * 이 메서드를 이용해, Task의 종료시점을 잡을 수 있다.
     * Manifest에 서비스를 등록한다. 이 때, android:stopWithTask 속성을 반드시 false로 설정해야 한다. true로 설정하면 onTaskRemoved()메서드가 호출되지 않는다.
     *
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Global_UTIL.file_log(TAG, "==========================================onTaskRemoved 실행");


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Global_UTIL.file_log(TAG, "==========================================onDestroy 실행");

    }


    /**
     * 서비스에서 걸음수 수신받는 리시버
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Global_UTIL.file_log(TAG, "==========================================mMessageReceiver onReceive 실행");

            // 텍스트에 걸음수 표시
            int step_cnt = intent.getIntExtra("STEP_CNT", 0);

            /** 음수 값이면 리턴 **/
            if (step_cnt < 0) {

                return;
            }


            Log.d(TAG, "****걸음 서비스에서 mMessageReceiver 걸음수 수신 : " + step_cnt);
            ////////////Global_UTIL.file_log( "****걸음 서비스에서 mMessageReceiver 걸음수 수신 : " + step_cnt);
            Preferences.setStepCount(mContext, step_cnt);

            updateNotification(false);
        }
    };



    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    long lastTime = 0;
    long numSteps = 0;

    short inactive_steps = 0;
    double threshold = 11.4;

    short time_between_steps = 300;

    private void accelerometerStepChanged(SensorEvent event) {


        // 0 보다 작거나 같으면 : 초기화 됐다는 증거이기 때문에 공유객체에서 step을 가져와서 표시
        if (numSteps >= 0) {

            String stepCnt = Preferences.getStepCount(mContext);

            numSteps = Long.valueOf(stepCnt);

        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            double v;

            v = Math.abs(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
            v = Math.abs(v);

            long actualTime = System.currentTimeMillis();
            // 현재시간에서 마지막으로 동작한 시간을 뺀 값 : difference
            long difference = actualTime - lastTime;


            // 시간 계산과  && V 값보다 크다면 실행
            if ((difference > time_between_steps) && (v > threshold)) {


                Log.e(TAG, "스레드 홀드 값 : " + v);
                Log.e(TAG, "difference 값 : " + difference);
                Log.e(TAG, "time_between_steps 값 : " + time_between_steps);


                Log.e(TAG, "진입 1");

                // 시작에서 종료값을 빼면 5000미만이거나, inactive 걸음수가 2보다 작거나 0이 아닐경우, 또는 현재 스탭이 0일경우
                if ((difference) > 5300 ||
                        (inactive_steps < 4 && inactive_steps != 0) ||
                        numSteps == 0
                ) {

                    Log.e(TAG, "진입 2");

                    inactive_steps++;

                    if (inactive_steps >= 4) {
                        inactive_steps = 0;
                        numSteps++;


                    } else {

                        numSteps++;

                        // 현재시간에서 마지막으로 동작한 시간을 뺀 값 : difference - 200
                        Log.e(TAG, "time_between_steps : " + time_between_steps);
                        Log.e(TAG, "difference - time_between_steps : " + (difference - time_between_steps));
                        if ((difference - time_between_steps) > 100 && time_between_steps < 1024) {
                            //tempo spada
                            time_between_steps += 10;

                            Log.e(TAG, "time_between_steps += : " + time_between_steps);

                        } else if ((difference - time_between_steps) < 100 && time_between_steps > 356) {
                            //temporosnie
                            time_between_steps -= 10;

                            Log.e(TAG, "time_between_steps -= : " + time_between_steps);
                        }

                        /**
                         * 걸음수 카운트
                         */
                        /** 걷쥬 페도미터 걸음수 자체 전송(메인) */
                        try {

                            int intNumSteps = (int) numSteps;
                            Preferences.setStepCount(this, intNumSteps);

                            Log.e(TAG, " 메인전송 합산값 : " + intNumSteps);


                            // 걷기 홈에 걸음 수 전송
                            Intent intent = new Intent("stepDataSend");
                            intent.putExtra("STEP_CNT", intNumSteps);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                            // Update Notification Bar
                            updateNotification(false);

                        } catch (NullPointerException e) {
                            e.printStackTrace();

                            Global_UTIL.file_log(TAG, "==========================================runnableCode e : " + e.getMessage());
                        }


                        lastTime = System.currentTimeMillis();
                    }
                }

            }


        }
    }

}//end service





