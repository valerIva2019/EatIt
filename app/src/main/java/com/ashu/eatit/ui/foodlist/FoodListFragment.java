package com.ashu.eatit.ui.foodlist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Adapter.MyCategoriesAdapter;
import com.ashu.eatit.Adapter.MyFoodListAdapter;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Common.SpacesItemDecoration;
import com.ashu.eatit.EventBus.MenuItemBack;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                new ViewModelProvider(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
            adapter = new MyFoodListAdapter(getContext(), foodModels);
            recycler_food_list.setAdapter(adapter);
            recycler_food_list.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }
    private void initViews() {

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}