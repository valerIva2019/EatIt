package com.ashu.eatit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Callback.IRecyclerClickListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.EventBus.CategoryClick;
import com.ashu.eatit.EventBus.MenuItemEvent;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.RestaurantModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {

    Context context;
    List<RestaurantModel> restaurantModelList;

    public MyRestaurantAdapter(Context context, List<RestaurantModel> restaurantModelList) {
        this.context = context;
        this.restaurantModelList = restaurantModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(restaurantModelList.get(position).getImageUrl())
                .into(holder.img_restaurant);
        holder.txt_restaurant_name.setText(new StringBuilder(restaurantModelList.get(position).getName()));
        holder.txt_restaurant_address.setText(new StringBuilder(restaurantModelList.get(position).getAddress()));

        //event
        holder.setListener((view, pos) -> {
            Common.restaurantSelected = restaurantModelList.get(pos);
            EventBus.getDefault().postSticky(new MenuItemEvent(true, restaurantModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return restaurantModelList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClickListener(view, getAdapterPosition());
        }
    }
}
