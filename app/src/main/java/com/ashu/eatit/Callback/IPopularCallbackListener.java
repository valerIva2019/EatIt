package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModelList);
    void onPopularLoadFailed(String message);
}
