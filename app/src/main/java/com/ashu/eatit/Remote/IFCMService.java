package com.ashu.eatit.Remote;


import com.ashu.eatit.Model.FCMResponse;
import com.ashu.eatit.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAPGCmMSI:APA91bEXeR-o6fuLtcHD6o91BU6npSsi8kpovD_Is_AoNmHDw7-za3SqBtqtcjCqX1V7AZzD9hcP7yugE3NrkPeEhKRFmdYGahkzzvC8i9xRUaKprC1iPf8lhNR3jNYZ-WSvaJpG__Zb"

    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
