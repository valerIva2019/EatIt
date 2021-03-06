package com.ashu.eatit.ui.view_orders;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidwidgets.formatedittext.widgets.FormatEditText;
import com.ashu.eatit.Adapter.MyOrdersAdapter;
import com.ashu.eatit.Callback.ILoadOrderCallbackListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Common.MySwiperHelper;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.CounterCartEvent;
import com.ashu.eatit.EventBus.MenuItemBack;
import com.ashu.eatit.Model.OrderModel;
import com.ashu.eatit.Model.RefundRequestModel;
import com.ashu.eatit.Model.ShippingOrderModel;
import com.ashu.eatit.R;
import com.ashu.eatit.TrackingOrderActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    private ViewOrdersViewModel viewOrdersViewModel;
    CartDataSource cartDataSource;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;
    public final static String TAG = "View Orders";
    LayoutAnimationController layoutAnimationController;

    private Unbinder unbinder;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ILoadOrderCallbackListener listener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrdersViewModel =
                new ViewModelProvider(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_orders, container, false);
        unbinder = ButterKnife.bind(this, root);


        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), orderList -> {
            Collections.reverse(orderList);
            Log.d(TAG, "onCreateView: o" + orderList.size());
            MyOrdersAdapter myOrdersAdapter = new MyOrdersAdapter(getContext(), orderList);
            recycler_orders.setAdapter(myOrdersAdapter);
            recycler_orders.setLayoutAnimation(layoutAnimationController);

        });

        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderModelList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(Common.restaurantSelected.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            OrderModel orderModel = orderSnapshot.getValue(OrderModel.class);
                            orderModel.setOrderNumber(orderSnapshot.getKey());
                            orderModelList.add(orderModel);
                        }
                        Log.d(TAG, "onCreateView: o" + orderModelList.size());
                        listener.onLoadOrderSuccess(orderModelList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initViews(View root) {
        listener = this;
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Cancel Order", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                            if (orderModel.getOrderStatus() == 0) {
                               if (orderModel.isCod()) {
                                   AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                   builder.setTitle("Cancel Order");
                                   builder.setMessage("Do you really want to cancel this order ?")
                                           .setPositiveButton("YES", (dialogInterface, i) -> {
                                               Map<String, Object> updateData = new HashMap<>();
                                               updateData.put("orderStatus", -1);
                                               FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                                       .child(Common.restaurantSelected.getUid())
                                                       .child(Common.ORDER_REF)
                                                       .child(orderModel.getOrderNumber())
                                                       .updateChildren(updateData)
                                                       .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid -> {
                                                   orderModel.setOrderStatus(-1);
                                                   ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos, orderModel);
                                                   recycler_orders.getAdapter().notifyItemChanged(pos);
                                                   Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
                                               });
                                           }).setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

                                   AlertDialog dialog = builder.create();
                                   dialog.show();
                               } else {
                                   View layout_refund_request = LayoutInflater.from(getContext()).inflate(R.layout.layout_refund_request, null);

                                   EditText edt_name = layout_refund_request.findViewById(R.id.edt_card_name);
                                   FormatEditText edt_card_number = layout_refund_request.findViewById(R.id.edt_card_number);
                                   FormatEditText edt_card_exp = layout_refund_request.findViewById(R.id.edt_exp);

                                   //Format credit card
                                   edt_card_number.setFormat("---- ---- ---- ----");
                                   edt_card_exp.setFormat("--/--");


                                   AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                   builder.setTitle("Cancel Order");
                                   builder.setMessage("Do you really want to cancel this order ?")
                                           .setView(layout_refund_request)
                                           .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
                                           .setPositiveButton("YES", (dialogInterface, i) -> {

                                               RefundRequestModel refundRequestModel = new RefundRequestModel();
                                               refundRequestModel.setName(Common.currentUser.getName());
                                               refundRequestModel.setPhone(Common.currentUser.getPhone());
                                               refundRequestModel.setCardName(edt_name.getText().toString());
                                               refundRequestModel.setCardNumber(edt_card_number.getText().toString());
                                               refundRequestModel.setCardExp(edt_card_exp.getText().toString());
                                               refundRequestModel.setAmount(orderModel.getFinalPayment());

/*
                                               String key = new StringBuilder().append(Common.currentUser.getPhone()).append("_")
                                                       .append(new Random().nextInt()).toString();*/
                                               FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                                       .child(Common.restaurantSelected.getUid())
                                                       .child(Common.REQUEST_REFUND_REF)
                                                       .child(orderModel.getOrderNumber())
                                                       .setValue(refundRequestModel)
                                                       .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                       .addOnSuccessListener(aVoid -> {
                                                           Map<String, Object> updateData = new HashMap<>();
                                                           updateData.put("orderStatus", -1);
                                                           FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                                                   .child(Common.restaurantSelected.getUid())
                                                                   .child(Common.ORDER_REF)
                                                                   .child(orderModel.getOrderNumber())
                                                                   .updateChildren(updateData)
                                                                   .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                                   .addOnSuccessListener(a -> {
                                                               orderModel.setOrderStatus(-1);
                                                               ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos, orderModel);
                                                               recycler_orders.getAdapter().notifyItemChanged(pos);
                                                               Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
                                                           });
                                                 /*  orderModel.setOrderStatus(-1);
                                                   ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos, orderModel);
                                                   recycler_orders.getAdapter().notifyItemChanged(pos);
                                                   Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();*/
                                               });
                                           });

                                   AlertDialog dialog = builder.create();
                                   dialog.show();
                               }
                            } else {
                                Toast.makeText(getContext(), "The order can't be cancelled now", Toast.LENGTH_SHORT).show();
                            }
                        }));
                buf.add(new MyButton(getContext(), "Tracking Order", 30, 0, Color.parseColor("#001970"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                            FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                    .child(Common.restaurantSelected.getUid())
                                    .child(Common.SHIPPING_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Common.currentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(snapshot.getKey());
                                                if (Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                        Common.currentShippingOrder.getCurrentLng() != -1) {
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                                                } else {
                                                    Toast.makeText(getContext(), "The shipper has not yet started the order", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "The order is not yet accepted by the restaurant", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
                buf.add(new MyButton(getContext(), "Repeat Order", 30, 0, Color.parseColor("#5d4037"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                            dialog.show();
                            cartDataSource.cleanCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            CartItem[] cartItems = orderModel.getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);
                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(() -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Added all items to cart", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
));
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };

    }


    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderModelList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList(orderModelList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}