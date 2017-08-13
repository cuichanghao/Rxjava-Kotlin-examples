package com.example.saityoukou.myapplication.kotlin.some;

import com.example.saityoukou.myapplication.kotlin.Api;
import com.example.saityoukou.myapplication.kotlin.RetrofitCreator;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoRequest;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoResponse;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoRequest;
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoResponse;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by cuichanghao007 on 2017/08/13.
 */

public class UserInfo {

    public UserInfo(UserBaseInfoResponse base, UserExtraInfoResponse extra) {
    }
}
