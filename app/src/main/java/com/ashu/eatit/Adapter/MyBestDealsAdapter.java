package com.ashu.eatit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashu.eatit.EventBus.BestDealItemClick;
import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.R;
import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealsAdapter extends LoopingPagerAdapter<BestDealModel> {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_best_deal)
    ImageView img_best_deal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_best_deal)
    TextView txt_img_deal;

    Unbinder unbinder;
    Context context;
    List<BestDealModel> itemList;

    public MyBestDealsAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
        this.context = context;
        this.itemList = itemList;
    }


    @Override
    protected View inflateView(int i, @NotNull ViewGroup viewGroup, int i1) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deals_item, viewGroup, false);
    }

    @Override
    protected void bindView(View view, int i, int i1) {
        unbinder = ButterKnife.bind(this, view);

        //set data
        Glide.with(view).load(itemList.get(i).getImage()).into(img_best_deal);
        txt_img_deal.setText(itemList.get(i).getName());

        view.setOnClickListener(view1 -> EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(i))));

    }
}
