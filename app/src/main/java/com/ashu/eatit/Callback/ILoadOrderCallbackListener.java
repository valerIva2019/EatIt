package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderModelList);
    void onLoadOrderFailed(String message);
}
