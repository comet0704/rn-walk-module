package com.ilevit.alwayz.android.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ilevit.alwayz.android.R;
import com.ilevit.alwayz.android.util.Preferences;

import java.text.DecimalFormat;
import java.util.Date;


/**
* 걸음 수 위젯
* @author 장지운
* @version 1.0.0
* @since  
**/
public class AppWidget extends AppWidgetProvider {

    private static final String TAG = "CnsWidgetProvider";
    private static final int WIDGET_UPDATE_INTERVAL = 5000;
    private static PendingIntent mSender;
    private static AlarmManager mManager;

    private Context mContext = null;

    private static final String SYNC_CLICKED    = "automaticWidgetSyncButtonClick";


    /**
     *
     * onEnabled : 위젯이 처음 생성될 때 호출된다.
     *
     * onDisabled : 위젯이 화면에서 삭제될 때 호출된다.
     *
     * onUpdate : 위젯 xml 파일 내의 android:updatePeriodMillis 값에 따라 주기적으로 호출된다. 위젯을 갱신하는 함수를 여기 넣으면 된다.(주기는 최소 30분으로 제한)
     *
     * onReceive : 브로드캐스트가 왔을 때 호출된다.
     *
     * @param context
     * @param intent
     */

    /* (non-Javadoc)
     * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        mContext = context;

        String action = intent.getAction();
        // 위젯 업데이트 인텐트를 수신했을 때
        if (action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
//            Log.w(TAG, "android.appwidget.action.APPWIDGET_UPDATE");
            removePreviousAlarm();

            long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mManager.set(AlarmManager.RTC, firstTime, mSender);
        }
        // 위젯 제거 인텐트를 수신했을 때
        else if (action.equals("android.appwidget.action.APPWIDGET_DISABLED")) {
//            Log.w(TAG, "android.appwidget.action.APPWIDGET_DISABLED");
            removePreviousAlarm();
        }
        // 리프레쉬 버튼 클릭시
        else if(SYNC_CLICKED.equals(intent.getAction())){

            // update 실행
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName testWidget = new ComponentName(context.getPackageName(), AppWidget.class.getName());
            int[] widgetIds = appWidgetManager.getAppWidgetIds(testWidget);
            if(widgetIds != null && widgetIds.length >0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), widgetIds);
            }


        }
    }

    /* (non-Javadoc)
     * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 현재 클래스로 등록된 모든 위젯의 리스트를 가져옴
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);

//            Toast.makeText(context, "onUpdate(): [" + String.valueOf(i) + "] " + String.valueOf(appWidgetId), Toast.LENGTH_SHORT).show();
        }

        // 버튼 클릭 등록
        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_app_widget);
        watchWidget = new ComponentName(context, AppWidget.class);

        remoteViews.setOnClickPendingIntent(R.id.ib_refrash, getPendingSelfIntent(context, SYNC_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);



    }

    /**
     * 클릭시 펜딩 인텐트를 통하여 onReceive 함수로 간다.
     * @param context
     * @param action
     * @return
     */
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    /**
     * 위젯의 형태를 업데이트합니다.
     *
     * @param context          컨텍스트
     * @param appWidgetManager 위젯 메니저
     * @param appWidgetId      업데이트할 위젯 아이디
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Date now = new Date();
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_app_widget);
//        updateViews.setTextViewText(R.id.widgettext, "[" + String.valueOf(appWidgetId) + "]" + now.toLocaleString());
        String stepCnt = Preferences.getStepCount(context);

        // 3자리 마다 콤마 표시
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        String formattedStringPrice = myFormatter.format(Integer.valueOf(stepCnt));


        updateViews.setTextViewText(R.id.widgettext, formattedStringPrice);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    /**
     * 예약되어있는 알람을 취소합니다.
     */
    public void removePreviousAlarm() {
        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }
}
