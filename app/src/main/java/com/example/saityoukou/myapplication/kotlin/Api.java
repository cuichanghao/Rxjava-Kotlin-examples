package com.example.saityoukou.myapplication.kotlin;

import com.example.saityoukou.myapplication.kotlin.requestresponse.LoginResponse;
import com.example.saityoukou.myapplication.kotlin.requestresponse.RegisterResponse;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoRequest;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoResponse;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoRequest;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;

/**
 * Created by cuichanghao007 on 2017/08/12.
 */

public interface Api {
    @GET
    Observable<LoginResponse> login(@Body String request);

    @GET
    Observable<RegisterResponse> register(@Body String request);

    @GET
    Observable<UserBaseInfoResponse> getUserBaseInfo(@Body UserBaseInfoRequest request);

    @GET
    Observable<UserExtraInfoResponse> getUserExtraInfo(@Body UserExtraInfoRequest request);

}

