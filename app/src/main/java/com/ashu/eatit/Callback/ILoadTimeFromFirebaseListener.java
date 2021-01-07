package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {

    void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs);
    void onLoadOnlyTimeSuccess(long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
