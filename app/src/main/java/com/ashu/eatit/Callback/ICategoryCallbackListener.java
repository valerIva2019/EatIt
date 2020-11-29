package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModels);
    void onCategoryLoadFailed(String message);
}
