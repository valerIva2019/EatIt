package com.ashu.eatit.ui.cart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Adapter.MyCartAdapter;
import com.ashu.eatit.Adapter.MyFoodListAdapter;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.HideFABCart;
import com.ashu.eatit.EventBus.UpdateItemInCart;
import com.ashu.eatit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;

    private Unbinder unbinder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartList().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems.isEmpty() || cartItems == null) {
                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
            } else {
                Log.e("TAG", "onChanged: here" );
                recycler_cart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                MyCartAdapter myCartAdapter = new MyCartAdapter(getContext(), cartItems);
                recycler_cart.setAdapter(myCartAdapter);
            }
        });
        unbinder = ButterKnife.bind(this, root);
        initViews();
        return root;
    }

    private void initViews() {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState); //fix error refresh recycler view after update
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "UPDATE CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NotNull Double price) {
                        txt_total_price.setText(new StringBuilder("Total : ")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "SUM CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}