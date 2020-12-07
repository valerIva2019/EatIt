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

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.OrderModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder> {

    private Context context;
    private List<OrderModel> orderModelList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrdersAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    public OrderModel getItemAtPosition(int pos) {
        return orderModelList.get(pos);
    }

    public void setItemAtPosition(int pos, OrderModel item){
        orderModelList.set(pos, item);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(orderModelList.get(position).getCartItemList().get(0).getFoodImg())
                .into(holder.img_order);
        calendar.setTimeInMillis(orderModelList.get(position).getCreateDate());
        Date date = new Date(orderModelList.get(position).getCreateDate());
        holder.txt_order_date.setText(new StringBuilder(Common.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
                .append(" ")
        .append(simpleDateFormat.format(date)));
        holder.txt_order_number.setText(new StringBuilder("Order number: ").append(orderModelList.get(position).getOrderNumber()));
        holder.txt_order_comment.setText(new StringBuilder("Comment: ").append(orderModelList.get(position).getComment()));
        holder.txt_order_status.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(orderModelList.get(position).getOrderStatus())));



    }


    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_order_comment)
        TextView txt_order_comment;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_order)
        ImageView img_order;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
