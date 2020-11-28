package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
