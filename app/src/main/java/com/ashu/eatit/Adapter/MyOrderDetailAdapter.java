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

import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Model.AddonModel;
import com.ashu.eatit.Model.SizeModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyOrderDetailAdapter.MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_order_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getFoodImg())
                .into(holder.img_food_image);
        holder.txt_food_name.setText(new StringBuilder().append(cartItemList.get(position).getFoodName()));
        holder.txt_food_quantity.setText(new StringBuilder("Quantity : ").append(cartItemList.get(position).getFoodQuantity()));
        SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(), new TypeToken<SizeModel>(){}.getType());
        if (sizeModel != null) {
            holder.txt_size.setText(new StringBuilder("Size : ").append(sizeModel.getName()));
        }
        if (!cartItemList.get(position).getFoodAddon().equals("Default")) {
            List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getFoodAddon(), new TypeToken<List<AddonModel>>(){}.getType());
            StringBuilder addOnString = new StringBuilder();
            if (addonModels != null) {
                for (AddonModel addonModel : addonModels) {
                    addOnString.append(addonModel.getName()).append(",");
                }
                addOnString.delete(addOnString.length() - 1,addOnString.length());
                holder.txt_food_add_on.setText(new StringBuilder("Addon : ").append(addOnString));
            }
        } else {
            holder.txt_food_add_on.setText(new StringBuilder("Addon : Default"));
        }

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_add_on)
        TextView txt_food_add_on;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_size)
        TextView txt_size;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }
}
