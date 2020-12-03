package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.Model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadOrderFailed(String message);
}
