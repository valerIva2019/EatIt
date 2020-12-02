package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.Order;

public interface ILoadTimeFromFirebaseListener {

    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
