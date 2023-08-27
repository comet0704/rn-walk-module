package com.ilevit.alwayz.android.util;

import android.os.Binder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogWrapper {
    private static final String TAG = "LogWrapper";
    private static final int LOG_FILE_SIZE_LIMIT = 512 * 1024;
    private static final int LOG_FILE_MAX_COUNT = 2;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS: ", Locale.getDefault());
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd");
    private static final Date date = new Date();
    private static final String LOG_FILE_NAME = "/CNS_LOG/FileLog%g" + fileNameFormat.format(date) + ".txt";
    private static Logger logger;
    private static FileHandler fileHandler;

    // TODO 사용법 LogWrapper.v(this.getClass().getSimpleName(), "***MainActivity************ onReceive() 변수 값 :    " + dsrSeq + "   /   " + carState + "   /   " + carStateFlag + "   /   " + procDtime);

	/**

	public static boolean FILE_LOG_DEBUG = false;

    public static void file_log(String TAG, String TEXT) {
        LogWrapper.v(TAG, TEXT);
    }

	if(Global.FILE_LOG_DEBUG == true){
		file_log("Drive", "getInstance");
    }

	*/

    static {
        try {
//            fileHandler = new FileHandler(Environment.getExternalStorageDirectory() + File.separator + LOG_FILE_NAME, LOG_FILE_SIZE_LIMIT, LOG_FILE_MAX_COUNT, true);

            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CNS_LOG";
            File file = new File(dirPath);

            // 일치하지 않은 폴더가 없으면 생성
            if(!file.exists()){
                file.mkdirs();
            }

            fileHandler = new FileHandler(Environment.getExternalStorageDirectory().getAbsolutePath() + LOG_FILE_NAME, LOG_FILE_SIZE_LIMIT, LOG_FILE_MAX_COUNT, true);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord r) {
                    date.setTime(System.currentTimeMillis());

                    StringBuilder ret = new StringBuilder(80);
                    ret.append(formatter.format(date));
                    ret.append(r.getMessage());
                    return ret.toString();
                }
            });
            logger = Logger.getLogger(LogWrapper.class.getName());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            Log.d(TAG, "init success");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "init failure");
        }
    }


    public static void v(String tag, String msg) {

        if (logger != null) {
            logger.log(Level.INFO, String.format("V/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), msg));
        }

        Log.v(tag, msg);
    }
}
