package com.ilevit.alwayz.android.util;

import android.content.Context;
import android.location.Location;

import com.ilevit.alwayz.android.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 공통으로 사용하는 전역 변수
 * Created by duck on 2016-03-08.
 */
public class Global {



    /**
     * 쉐어드 프리퍼런스 객체
     */
    public static final String SHARED_PREFERENCE_NAME = "always";

    public static Context gContext;
    public static MainActivity gMainActivity;

    /**
     * 로그인 정보 관련
     */
    public static String MEM_MYTODAYSTEP = "0";
    public static boolean MEM_LOGOUT_CHECK = false;




}
