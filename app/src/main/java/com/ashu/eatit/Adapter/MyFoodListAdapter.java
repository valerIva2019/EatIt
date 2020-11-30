package com.ashu.eatit.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Callback.IRecyclerClickListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.EventBus.FoodItemClick;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("$")
        .append(foodModelList.get(position).getPrice()));
        holder.txt_food_name.setText(new StringBuilder("")
        .append(foodModelList.get(position).getName()));

        //event
        holder.setListener((view, pos) -> {
            Common.selectedFood = foodModelList.get(pos);
            EventBus.getDefault().postSticky(new FoodItemClick(true, foodModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private Unbinder unbinder;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_quick_cart)
        ImageView img_cart;

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
