package com.ashu.eatit.Remote;

import com.ashu.eatit.Model.BrainTreeToken;
import com.ashu.eatit.Model.BrainTreeTransaction;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ICloudFunctions {
    @GET("token")
    Observable<BrainTreeToken> getToken();

    @POST("checkout")
    @FormUrlEncoded
    Observable<BrainTreeTransaction> submitPayment(@Field("amount") double amount,
                                                   @Field("payment_method_nonce") String nonce);
}
