package com.ilevit.alwayz.android.workmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ilevit.alwayz.android.MainActivity;
import com.ilevit.alwayz.android.net.retrofit.RetrofitCallBack;
import com.ilevit.alwayz.android.net.retrofit.RetrofitClient;
import com.ilevit.alwayz.android.net.retrofit.model.StepDataModel;
import com.ilevit.alwayz.android.net.retrofit.service.StepService;
import com.ilevit.alwayz.android.service.NotificationStepService;
import com.ilevit.alwayz.android.util.Global_UTIL;
import com.ilevit.alwayz.android.util.Preferences;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 반복되는 작업은 PeriodicWorkRequestBuilder 를 이용하여 PeriodicWorkRequest 객체를 생성하여 WorkManager 의 큐에 추가 하면 됩니다.
 * 이때 첫번째 인자로는 반복될 인터벌 값, 두번째 인자로는 이 인터벌의 시간타입이 필수로 들어가야 합니다.
 * 다음과 같은 예시는 15분 반복을 뜻하며 TimeUnit 에정의된 다른 시간 타입 enum 을 사용할수도 있습니다.
 */

/**
 * •Worker : 추상 클래스 입니다. 처리해야 하는 백그라운드 작업의 처리 코드를 이 클래스를 상속받아 doWork() 메서드를 오버라이드 하여 작성하게 됩니다.
 * •WorkState : WorkRequest 의 id 와, 해당 WorkRequest 의 현재 상태를 담는 클래스입니다.
 * 개발자는 WorkState 의 상태 정보를 이용해서 자신이 요청한 작업의 현재 상태를 파악할수 있습니다.
 * WorkState 는 ENQUEUED, RUNNING, SUCCEEDED, FAILED, BLOCKED, CANCELLED 의 6개의 상태를 가집니다.
 */
public class NotificationWorker extends Worker {

    private static final String TAG = NotificationWorker.class.getSimpleName();

    private static final String WORK_RESULT = "work_result";

    Context mContext = null;
    private Retrofit retrofitClient = null;


    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        mContext = context;

        if (retrofitClient == null) {
            retrofitClient = RetrofitClient.getInstance().getRetrofit();
        }
    }

    /**
     * doWork() 메서드는 작업을 완료하고 결과에 따라 Worker 클래스 내에 정의된 enum 인 Result 의 값중 하나를 리턴해야 합니다. SUCCESS, FAILURE, RETRY 의 3개 값이 있으며 리턴되는 값에 따라
     * WorkerManager 는 해당 작업을 마무리 할것인지 재시도 할것인지, 실패로 정의하고 중단할것인지 이후 동작을 결정하게 됩니다.
     *
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
        String taskDataString = taskData.getString(MainActivity.JOB_MESSAGE_STATUS);


//        showNotification("WorkManager", taskDataString != null ? taskDataString : "Message has been Sent");

        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();


        //////////Global_UTIL.file_log( "==========================================워크 매니져 doWork : " + taskDataString);


        boolean isService = isServiceRunningCheck();
        if (isService == false) {
            // TODO 서비스가 동작하지 않는다면 서비스 재시작

            Intent foregroundServiceIntent = new Intent(mContext, NotificationStepService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(foregroundServiceIntent);
            } else {
                mContext.startService(foregroundServiceIntent);
            }
        }


        getBandData();

        setCheckDay();


        return Result.success(outputData);

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean isServiceRunningCheck() {

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Log.e(TAG, service.service.getClassName().toString());
            if ("com.ilevit.alwayz.android.service.NotificationStepService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void getBandData() {

        try {

//            Global_UTIL.file_log(TAG, "==========================================워크매니져 스탭 서비스 재실행");

            Intent foregroundServiceIntent = new Intent(mContext, NotificationStepService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(foregroundServiceIntent);
            } else {
                mContext.startService(foregroundServiceIntent);
            }

        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }catch (Exception e){
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
            Preferences.setSendDate(mContext, nowDate);

            return;
        }

        /** 오늘 날짜와 걸음 전송한 날짜가 다르면 걸음수 초기화가 안된것으로 파악하여 걸음수 초기화 안된것으로 파악하고 , 서버에 걸음수 0 전송 */
        if (!nowDate.equalsIgnoreCase(preDate)) {
            // 날짜가 틀리면 걸음수 초기화 하고, 서버에 걸음수 전송한다.

            midnightExecute();

        }


    }

    /**
     * 걸음수 0 초기화 하고 걸음수 0 서버에 전송
     */
    private void midnightExecute() {


        if (mContext == null) {
            return;
        }

        //////////Global_UTIL.file_log( "==========================================워크매니져 midnightExecute start");


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


        /**
         * (1) 자정이 되면 서버에 폴링해서 SEND해서 걸음수 저장 값 초기화
         */
        Preferences.setSendStepCount(mContext, 0);

        // 걷기 홈과 노티에  현재 걸음 수 전송하여 보여지는 화면 업데이트
        Intent midnightIntent = new Intent("stepDataSend");
        midnightIntent.putExtra("STEP_CNT", 0);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(midnightIntent);


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


                ////////////Global_UTIL.file_log( "==========================================워크매니져 midnightExecute end : " + nowDate);
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


//    private void showNotification(String task, String desc) {
//
//        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        String channelId = "task_channel";
//        String channelName = "task_name";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//            manager.createNotificationChannel(channel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
//                .setContentTitle(task)
//                .setContentText(desc)
//                .setSmallIcon(R.mipmap.ic_launcher);
//
//        manager.notify(1, builder.build());
//
//    }

}
