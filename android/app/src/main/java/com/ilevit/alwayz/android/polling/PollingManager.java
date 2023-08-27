package com.ilevit.alwayz.android.polling;

import android.content.Context;

import com.ilevit.alwayz.android.polling.run.StepDataRunnable;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PollingManager {

    public static final String ACTION_POLLING_STEP_DATA = "com.ilevit.alwayz.android.polling.step";


    /**
     * 폴링(걸음 데이터 전송) 간격<br/><br/>
     * 단위: second
     */
    public static final int POLLING_START_STEP_DATA_DELAY = 5;
    //    public static final int POLLING_INTERVAL_STEP_DATA_DELAY = 60 * 5; // 5분
    public static final int POLLING_INTERVAL_STEP_DATA_DELAY = 20; // 20초


    /**
     * single thread pool for polling
     */
    public static ScheduledExecutor _scheduled_executor = null;

    // 걷기
    public static ScheduledFuture<?> _scheduler_step_scheduled = null;
    public static StepDataRunnable _step_data_Runnable = null;


    /**
     * 걷기 데이터 주기적으로 서버에 전송
     */
    public static void pollingStepData(Context context) {


        try {

            if (_scheduler_step_scheduled == null) {
                _step_data_Runnable = new StepDataRunnable(context);
                _scheduler_step_scheduled = _scheduled_executor.fScheduleAtFixedRate(_step_data_Runnable, POLLING_START_STEP_DATA_DELAY, POLLING_INTERVAL_STEP_DATA_DELAY, TimeUnit.SECONDS);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }



    // {{ 폴링 취소 START

    /**
     * 걷기 전송 폴링 취소
     */
    public static void cancelPollingStepData() {

        if (_scheduler_step_scheduled != null) {
            _scheduler_step_scheduled.cancel(false);
        }

        // 초기화
        _scheduler_step_scheduled = null;
        _step_data_Runnable = null;
    }


}
