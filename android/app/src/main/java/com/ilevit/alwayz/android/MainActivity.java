package com.ilevit.alwayz.android;
import static com.ilevit.alwayz.android.util.Global.gContext;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import org.devio.rn.splashscreen.SplashScreen;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.ilevit.alwayz.android.receiver.DeviceDateEventReceiver;
import com.ilevit.alwayz.android.service.NotificationStepService;
import com.ilevit.alwayz.android.service.NotificationStepService;
import com.ilevit.alwayz.android.util.Global;
import com.ilevit.alwayz.android.util.Global_UTIL;
import com.ilevit.alwayz.android.util.Preferences;

import java.util.concurrent.TimeUnit;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */

  public static final String JOB_MESSAGE_STATUS = "mainActiivtyStart";

  // step Service 인텐트
  Intent foregroundServiceIntent;
  // 싱글톤 메인 공유 객체
  private static MainActivity mainActivity = new MainActivity();

  /**
   * 삼성헬스 자정 초기화 리시버
   */
  private BroadcastReceiver ready_receiver = null;

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // SplashScreen.show(this);
    super.onCreate(null);

    Global.gMainActivity = this;

  }

  public  void onInit() {
//    Preferences.setStepCount(gContext, 15);

    // step 서비스 실행
    setMidnightAlarm();

        // step 서비스 실행
        stepServiceStart();

        //
        readyReceiver();
  }


  /**
   * 자정 알림 알람 설정
   */
  public void setMidnightAlarm() {

    // 알람 설정 확인
    Intent intent = new Intent(gContext, DeviceDateEventReceiver.class);
    // PendingIntent.FLAG_NO_CREATE 플래그를 사용하여 PendingIntent.getActivity, PendingIntent.getBroadcast, PendingIntent.getService 등을 호출해 보면 된다. 이미 설정된 알람이 없다면 null 을 반환한다.
    PendingIntent pIntent = PendingIntent.getBroadcast(gContext, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
    // result == true 이면 설정되지 않았음
    boolean result = (pIntent == null);

    // 자정 알람 설정
    if (result == true) {
//      Log.d(TAG, "onCreate 자정 알림 알람 설정 ON");
      Global_UTIL.resetAlarm(gContext);
    } else {
//      Log.d(TAG, "onCreate 자정 알림 알람 설정 OFF");


      String nowDate = Global_UTIL.getNowDate();
      String alarmDate = "";

      alarmDate = Preferences.getAlarmDate(gContext);

      if (nowDate.equalsIgnoreCase(alarmDate)) {

        // TODO : 이미 지난 알림이라면 취소하고(알람이 오늘 날짜이면 취소)
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent cancelIntent = new Intent(gContext, DeviceDateEventReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (sender != null) {
          am.cancel(sender);
          sender.cancel();

          // 취소하고 다시 알람을 맞추기 위해서 다시 호출
          setMidnightAlarm();
        }
      }//end if
    }

  }

  /**
   * 자정시 DeviceDateEventReceiver에서 호출
   */
  private void readyReceiver() {

    Log.d(this.getClass().getSimpleName(), "readyReceiver");

    if (ready_receiver == null) {

      IntentFilter filter = new IntentFilter("com.ilevit.alwayz.android.midnight");
      ready_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

          if (intent.getAction().equalsIgnoreCase("com.ilevit.alwayz.android.midnight")) {

            String whatBandConn = Preferences.getStepConnWhat(context);


            // 걷기 홈과 노티에  현재 걸음 수 전송하여 보여지는 화면 업데이트
            Intent midnightIntent = new Intent("stepDataSend");
            midnightIntent.putExtra("STEP_CNT", 0);
            LocalBroadcastManager.getInstance(context).sendBroadcast(midnightIntent);
          }

        }
      };//end receiver
      registerReceiver(ready_receiver, filter);
    }
  }


  /**
   * 포그라운드 서비스 시작
   */
  private void stepServiceStart() {

    // 영원이 도는 걷기 노티피케이션 서비스 동작중인지 체크
    boolean stepNotificationService = isServiceRunning();
    if (Global.MEM_LOGOUT_CHECK == true) {
      stepNotificationService = false;
    }
    if (stepNotificationService == false) {
      foregroundServiceIntent = new Intent(gContext, NotificationStepService.class);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startService(foregroundServiceIntent);
                Log.d(TAG, "stepServiceStart : startForegroundService  서비스 실행");
      } else {
        startService(foregroundServiceIntent);
                Log.d(TAG, "stepServiceStart : startService  서비스 실행");
      }
    } else {

      // 사용자가 사용하다가 로그아웃 했을 경우 다시 로그인하고 실행했을 경우 노티피케이션 업데이트를 하기위함
      int nowCnt = Integer.valueOf(Preferences.getStepCount(gContext));
      Intent intent = new Intent("stepDataSend");
      intent.putExtra("STEP_CNT", nowCnt);
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

  }

  public boolean isServiceRunning() {
    ActivityManager manager = (ActivityManager) gContext.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (NotificationStepService.class.getName().equals(service.service.getClassName()))
        return true;
    }
    return false;
  }

  @Override
  protected String getMainComponentName() {
    return "Alwayz";
  }

  /**
   * Returns the instance of the {@link ReactActivityDelegate}. There the RootView is created and
   * you can specify the rendered you wish to use (Fabric or the older renderer).
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new MainActivityDelegate(this, getMainComponentName());
  }
  
  public static class MainActivityDelegate extends ReactActivityDelegate {
    public MainActivityDelegate(ReactActivity activity, String mainComponentName) {
      super(activity, mainComponentName);
    }

    @Override
    protected ReactRootView createRootView() {
      ReactRootView reactRootView = new ReactRootView(getContext());
      // If you opted-in for the New Architecture, we enable the Fabric Renderer.
      reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
      return reactRootView;
    }
    @Override
    protected boolean isConcurrentRootEnabled() {
      // If you opted-in for the New Architecture, we enable Concurrent Root (i.e. React 18).
      // More on this on https://reactjs.org/blog/2022/03/29/react-v18.html
      return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
    }
  }
}
