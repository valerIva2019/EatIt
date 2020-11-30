package com.ashu.eatit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Callback.IRecyclerClickListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.EventBus.FoodItemClick;
import com.ashu.eatit.Model.CommentModel;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.MyViewHolder> {

    private Context context;
    private List<CommentModel> commentModelList;

    public MyCommentAdapter(Context context, List<CommentModel> commentModelList) {
        this.context = context;
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Long timeStamp = Long.parseLong(commentModelList.get(position).getServerTimeStamp().get("timeStamp").toString());
        holder.txt_comment_date.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
        holder.txt_comment.setText(commentModelList.get(position).getComment());
        holder.txt_comment_name.setText(commentModelList.get(position).getName());
        holder.ratingBar.setRating(commentModelList.get(position).getRatingValue());
    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private Unbinder unbinder;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_comment_date)
        TextView txt_comment_date;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_comment)
        TextView txt_comment;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_comment_name)
        TextView txt_comment_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.rating_bar)
        RatingBar ratingBar;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
