package com.ashu.eatit.ui.fooddetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.CommentModel;
import com.ashu.eatit.Model.FoodModel;

import java.util.List;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveDataFood;
    private MutableLiveData<CommentModel> mutableLiveDataComment;

    public FoodDetailViewModel() {

    }

    public void setCommentModel (CommentModel commentModel) {

        if (mutableLiveDataComment != null)
            mutableLiveDataComment.setValue(commentModel);

    }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {
        if (mutableLiveDataComment == null)
            mutableLiveDataComment = new MutableLiveData<>();

        return mutableLiveDataComment;
    }

    public MutableLiveData<FoodModel> getMutableLiveDataFood() {
        if (mutableLiveDataFood == null)
            mutableLiveDataFood = new MutableLiveData<>();
        mutableLiveDataFood.setValue(Common.selectedFood);

        return mutableLiveDataFood;
    }

    public void setFoodModel(FoodModel foodModel) {
        if (mutableLiveDataFood != null)
            mutableLiveDataFood.setValue(foodModel);
    }
}