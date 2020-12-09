package com.ashu.eatit.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Adapter.MyBestDealsAdapter;
import com.ashu.eatit.Adapter.MyPopularCategoriesAdapter;
import com.ashu.eatit.R;
import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Unbinder unbinder;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.viewpager)
    LoopingViewPager viewPager;

    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, root);

        String key = getArguments().getString("restaurant");

        init();
        homeViewModel.getPopularList(key).observe(getViewLifecycleOwner(), popularCategoryModels -> {
            //create adapter
            MyPopularCategoriesAdapter myPopularCategoriesAdapter = new MyPopularCategoriesAdapter(getContext(), popularCategoryModels);
            recycler_popular.setAdapter(myPopularCategoriesAdapter);
            recycler_popular.setLayoutAnimation(layoutAnimationController);
        });

        homeViewModel.getBestDealList(key).observe(getViewLifecycleOwner(), bestDealModels -> {
            //create adapter
            MyBestDealsAdapter adapter = new MyBestDealsAdapter(getContext(), bestDealModels, true);
            viewPager.setAdapter(adapter);

        });
        return root;
    }

    private void init() {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();

    }
}