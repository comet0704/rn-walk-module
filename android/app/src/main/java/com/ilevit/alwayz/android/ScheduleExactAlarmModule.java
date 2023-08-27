package com.ilevit.alwayz.android;

import static com.ilevit.alwayz.android.util.Global.gContext;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

public class ScheduleExactAlarmModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public ScheduleExactAlarmModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        gContext = reactContext;
    }

    @Override
    public String getName() {
        return "ScheduleExactAlarm";
    }

    @ReactMethod
    public void checkPermission(Promise promise) {
        AlarmManager alarmManager = (AlarmManager) reactContext.getSystemService(Context.ALARM_SERVICE);
        boolean hasPermission = alarmManager.canScheduleExactAlarms();
        promise.resolve(hasPermission);
    }

    @ReactMethod
    public void requestPermission() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }
}
