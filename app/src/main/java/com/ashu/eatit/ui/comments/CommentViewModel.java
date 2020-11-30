package com.ashu.eatit.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.CommentModel;
import com.ashu.eatit.Model.FoodModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataCommentList;

    public CommentViewModel() {
        mutableLiveDataCommentList = new MutableLiveData<>();

    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList() {
        return mutableLiveDataCommentList;
    }

    public void setComment(List<CommentModel> commentList) {
        mutableLiveDataCommentList.setValue(commentList);
    }
}
