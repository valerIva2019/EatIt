package com.ashu.eatit.Remote;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitICloudClient {

    private static Retrofit instance;
    public static Retrofit getInstance(String paymentUrl) {

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher("string1234more567string890");

        if (instance == null)
            instance = new Retrofit.Builder()
                    .baseUrl("https://us-central1-eatit-7f146.cloudfunctions.net/widgets/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            return instance;
    }
}
