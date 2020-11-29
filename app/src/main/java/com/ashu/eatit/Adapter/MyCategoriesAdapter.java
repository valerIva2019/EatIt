package com.ashu.eatit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.PopularCategoryModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyCategoriesAdapter extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MyCategoriesAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(categoryModelList.get(position).getImage())
                .into(holder.category_image);
        holder.category_name.setText(new StringBuilder(categoryModelList.get(position).getName()));
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_category)
        ImageView category_image;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_category)
        TextView category_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (categoryModelList.size() == 1)
             return Common.DEFAULT_COLUMN_COUNT;
        else {
            if (categoryModelList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == categoryModelList.size() - 1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
        }

    }
}
