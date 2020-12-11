package com.ashu.eatit.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.Model.FoodModel;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItem>> mutableLiveDataCartList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();

    }
    public void initCartDataSource(Context context) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartList() {
        if (mutableLiveDataCartList == null)
            mutableLiveDataCartList = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartList;
    }

    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::accept, throwable -> mutableLiveDataCartList.setValue(null)));
    }

    private void accept(List<CartItem> cartItems) {
        mutableLiveDataCartList.setValue(cartItems);
    }
}