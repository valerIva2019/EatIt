package com.ashu.eatit.ui.fooddetail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andremion.counterfab.CounterFab;
import com.ashu.eatit.Adapter.MyFoodListAdapter;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FoodDetailFragment extends Fragment {

    private FoodDetailViewModel foodDetailViewModel;
    private Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_food)
    ImageView img_food;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.food_name)
    TextView food_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.food_description)
    TextView food_description;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.food_price)
    TextView food_price;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btnShowComment)
    Button btnShowComment;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                new ViewModelProvider(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);

        unbinder = ButterKnife.bind(this, root);

        foodDetailViewModel.getMutableLiveDataFood().observe(getViewLifecycleOwner(), this::displayInfo);
        return root;
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());

    }
}