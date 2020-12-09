package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.PopularCategoryModel;
import com.ashu.eatit.Model.RestaurantModel;

import java.util.List;

public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);
}
