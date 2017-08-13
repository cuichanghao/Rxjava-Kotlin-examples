package com.example.saityoukou.myapplication.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast

import com.example.saityoukou.myapplication.R
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoRequest
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserBaseInfoResponse
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoRequest
import com.example.saityoukou.myapplication.kotlin.requestresponse.UserExtraInfoResponse
import com.example.saityoukou.myapplication.kotlin.some.UserInfo
import io.reactivex.*
import io.reactivex.Observable.zip

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    internal var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFlowable2()
        var btn = findViewById(R.id.rxjava) as Button
        btn.setOnClickListener { testFlowable() }
    }

    fun testObserver(){
        //创建一个上游 Observable：
        val observable = Observable.create(ObservableOnSubscribe<Int> { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete()
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Int> {
                    override fun onSubscribe(d: Disposable) {
                        Log.d(TAG, "subscribe")
                    }

                    override fun onNext(value: Int) {
                        Log.d(TAG, "" + value!!)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(TAG, "error")
                    }

                    override fun onComplete() {
                        Log.d(TAG, "complete")
                    }
                })
    }


    fun testMap(){
        //创建一个上游 Observable：
        val observable = Observable.create(ObservableOnSubscribe<Int> { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete()
        }).map{"This is result " + it } //Function<? super T, ? extends R>簡略
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Log.d(TAG, it) }) //Consumer<String>()の簡略
    }

    fun testFlatMap(){
        //创建一个上游 Observable：
        val observable = Observable.create(ObservableOnSubscribe<Int> { emitter ->

            for (i in 0..100){
                emitter.onNext(i)
            }
            emitter.onComplete()
        }).flatMap({ emitValue -> //Function<Int, ObservableSource<String>>
            val list = mutableListOf<String>() //javaのArrayList<String>()と同じ
            (0..2).mapTo(list) { "I am value " + emitValue } //for (i in 0..2)の代わりに使う。
            Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Log.d(TAG, it) }) //Consumer<String>()の簡略
    }

    //mapもしくはflatMapを使ってひapi1->api2のようにchain実行可能
    fun testApiChain(){
        val retrofit = RetrofitCreator.create()
        val api = retrofit.create(Api::class.java)

        api.register("name")
                .subscribeOn(Schedulers.io()) //別スレッドでApi call
                .observeOn(AndroidSchedulers.mainThread()) //MainThreadでresponse処理をする
                .doOnNext { registerResponse ->
                    //registerの結果によって何かを処理する
                }
                .observeOn(Schedulers.io()) //別スレッド
                .flatMap ({ registerResponse ->// registerResponseをなんらかのListに変えるのもできるがここでは1:1か
                    Log.d(TAG, registerResponse.toString()) //user registerResponseの情報を使って何かをする
                    api.login("loginName, Password") //chain,observableを返せばいいので,return Observable.from(registerResponse.getSomeList())のような感じでもいい。
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show() }, //response
                        { Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show() }) //error
    }

    fun testZip0(){
        //创建一个上游 Observable：
        val observable1 = Observable.create(ObservableOnSubscribe<Int> { emitter ->
            for (i in 0..1000000){
                emitter.onNext(i)
            }
            emitter.onComplete()
        }).subscribeOn(Schedulers.io()) //これがないと同期になってしますので並列実行にならない

        val observable2 = Observable.create(ObservableOnSubscribe<String> { emitter ->
            for (i in 0..1000000){
                emitter.onNext("A${i}A-")
            }
            emitter.onComplete()
        }).subscribeOn(Schedulers.io()) //これがないと同期になってしますので並列実行にならない

        Observable.zip(observable1, observable2, BiFunction<Int, String, String> { int, str -> str.plus(int) })
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {
                        Log.d(TAG, "subscribe")
                    }

                    override fun onNext(value: String) {
                        Log.d(TAG, "" + value)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(TAG, "error")
                    }

                    override fun onComplete() {
                        Log.d(TAG, "complete")
                    }
                })
    }

    fun testZip(){
        val retrofit = RetrofitCreator.create()
        val api = retrofit.create(Api::class.java)
        //Api１
        val observable1 = api.getUserBaseInfo(UserBaseInfoRequest()).subscribeOn(Schedulers.io())
        //Api2
        val observable2 = api.getUserExtraInfo(UserExtraInfoRequest()).subscribeOn(Schedulers.io())
        //Api1とApi2を同時に実行してその結果からUserInfoを作成する。最終結果は少ない方になる。5,2だと2個しか結果がない。
        Observable.zip(observable1, observable2, BiFunction<UserBaseInfoResponse, UserExtraInfoResponse, UserInfo> { userBase, userExtra ->
            UserInfo(userBase, userExtra)
        })
    }

    var subscription: Subscription? = null
    fun testFlowable(){
        subscription?.request(96)
    }
    fun setFlowable(){
        Flowable.create(FlowableOnSubscribe<Int> { emitter ->
            for (i in 0..10000){//Flowableのバッファサイズ128より大きく処理仕切れない場合はMissingBackpressureExceptionになる
//                Log.d(TAG, "emit " + i);
                emitter.onNext(i)
            }
        }, BackpressureStrategy.LATEST) //処理しきれないときに無視する,ただ一番最後の状態を取得する
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : Subscriber<Int> {
            override fun onSubscribe(d: Subscription) {
                Log.d(TAG, "subscribe")
                subscription = d
//                d.request(100) //処理仕切れない100以後は無視する
            }

            override fun onNext(value: Int) {
                Log.d(TAG, "" + value)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                Log.d(TAG, "complete")
            }
        })
    }

    fun setFlowable2(){
        Flowable.create(FlowableOnSubscribe<Int> { emitter ->
            Log.d(TAG, "First requested = " + emitter.requested());
            var flag: Boolean
            var i: Int = 0
            while (true) {
                flag = false;
                while (emitter.requested() == 0L) {
                    if (!flag) {
                        Log.d(TAG, "Oh no! I can't emit value!");
                        flag = true;
                    }
                }
                emitter.onNext(i);
                i = i+1
                Log.d(TAG, "emit " + i + " , requested = " + emitter.requested());
            }
        }, BackpressureStrategy.LATEST) //処理しきれないときに無視する,ただ一番最後の状態を取得する
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : Subscriber<Int> {
            override fun onSubscribe(d: Subscription) {
                Log.d(TAG, "subscribe")
                subscription = d
//                d.request(100) //処理仕切れない100以後は無視する
            }

            override fun onNext(value: Int) {
                Log.d(TAG, "" + value)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                Log.d(TAG, "complete")
            }
        })
    }
}
