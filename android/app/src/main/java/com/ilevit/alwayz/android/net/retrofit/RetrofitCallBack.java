package com.ilevit.alwayz.android.net.retrofit;

import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RetrofitCallBack<T> implements Callback<T> {

    private static final String TAG = RetrofitCallBack.class.getSimpleName();

    private final int RESPONSE_CODE_FAIL = -1;


    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        // 결과값 로그
//        responseDataLog(response);

        // 성공
        if (response.isSuccessful()) {
            onSuccess(response.body(), response.raw().request().url(), response);
        } else {
            // 실패

            String errorMsg = "";
            errorMsg = disposeEorCode(response.code());
            onFail(response.code(), errorMsg, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String tmp = "";
        try{
            tmp = t.getMessage().toString();
        }catch (NullPointerException e){
            e.printStackTrace();

            tmp = "";
        }

        Log.d(TAG, "\n" + ">>> onFailure 호출 실패 : " + tmp + "\n");
        String errorMessage = "서버에서 데이터를 가져오는데 실패 했습니다." + tmp;

        if (t instanceof SocketTimeoutException) {
            errorMessage = "서버 응답 시간이 초과되었습니다.";
        } else if (t instanceof ConnectException) {
            errorMessage = "네트워크 연결에 실패하였습니다.";
        } else if (t instanceof RuntimeException) {
            errorMessage = "런타임 에러 발생";
        } else if (t instanceof UnknownHostException) {
            errorMessage = "네트워크 연결을 확인 할 수 없습니다.";
        } else if (t instanceof UnknownHostException) {
            errorMessage = "서버에서 알 수 없는 오류가 발생하였습니다.";
        }

//        onFail(RESPONSE_CODE_FAIL, errorMessage);
        onNetworkFail(RESPONSE_CODE_FAIL, errorMessage);
    }

    private String disposeEorCode(int code) {
        String errorMeessage = "";
        switch (code) {
            case 101:
                errorMeessage = "요청자가 서버에 프로토콜 전환을 요청했으며 서버는 이를 승인하는 중입니다.";
                break;
            case 401:
                errorMeessage = " 이 요청은 인증이 필요하다.";
                break;
            case 404:
                errorMeessage = "서버가 요청한 페이지(Resource)를 찾을 수 없습니다.";
                break;
            case 307:
                errorMeessage = "네트워크 상태가 불안정 합니다. 다시 요청해 주십시오."; // 현재 서버가 다른 위치의 페이지로 요청에 응답하고 있지만 요청자는 향후 요청 시 원래 위치를 계속 사용해야 한다
                break;
            case 400:
                errorMeessage = "잘못된 요청 오류 발생";
                break;
            case 500:
                // 서버 오류 발생
                errorMeessage = "조회에 실패 하였습니다. 다시 시도해 주시기 바랍니다.";
                break;

        }

        return errorMeessage;
    }


    /**
     * 서버에서 받은 데이터 로그 출력
     */
    private void responseDataLog(Response<T> response) {
        try {
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>응답 결과 : " + response.body().toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public abstract void onSuccess(T responseValue, HttpUrl url, Response<T> extraObject);

    public abstract void onNetworkFail(int code, String message);

    public abstract void onFail(int code, String message,  Response<T> extraObject);
}
