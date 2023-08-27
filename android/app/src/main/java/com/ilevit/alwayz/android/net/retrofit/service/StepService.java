package com.ilevit.alwayz.android.net.retrofit.service;

import com.ilevit.alwayz.android.net.retrofit.model.StepDataModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface StepService {


    /**
     * 걸음 데이터 전송 정보 서버에 INSERT
     *
     * @param model
     * @return
     */
    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("api/steps")
    Call<ResponseBody> setSteps_post(@Header("Authorization") String token, @Body StepDataModel model);



}
