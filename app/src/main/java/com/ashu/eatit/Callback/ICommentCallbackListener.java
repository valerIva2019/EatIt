package com.ashu.eatit.Callback;

import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
