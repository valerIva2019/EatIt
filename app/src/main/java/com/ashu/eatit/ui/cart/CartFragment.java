package com.ashu.eatit.ui.cart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Adapter.MyCartAdapter;
import com.ashu.eatit.Adapter.MyFoodListAdapter;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Common.MySwiperHelper;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.CounterCartEvent;
import com.ashu.eatit.EventBus.HideFABCart;
import com.ashu.eatit.EventBus.UpdateItemInCart;
import com.ashu.eatit.Model.BrainTreeTransaction;
import com.ashu.eatit.Model.Order;
import com.ashu.eatit.R;
import com.ashu.eatit.Remote.ICloudFunctions;
import com.ashu.eatit.Remote.RetrofitICloudClient;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private static final int REQUEST_BRAINTREE_CODE = 7777;
    private CartViewModel cartViewModel;
    String address, comment;
    ICloudFunctions cloudFunctions;

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

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

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step!!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = view.findViewById(R.id.edt_address);
        EditText edt_comment = view.findViewById(R.id.edt_comment);
        TextView txt_address = view.findViewById(R.id.txt_address_detail);

        RadioButton rdi_home = view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_this_address = view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = view.findViewById(R.id.rdi_braintree);

        edt_address.setText(Common.currentUser.getAddress());

        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);

            }
        });
        rdi_other_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText("");
                txt_address.setVisibility(View.GONE);

            }
        });
        rdi_ship_this_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        })
                        .addOnCompleteListener(task -> {
                            String coordinates = task.getResult().getLatitude() +
                                    "/" +
                                    task.getResult().getLongitude();

                            Single<String> singleAddress = Single.just(getAddressFromLatLnng(task.getResult().getLatitude(),
                                    task.getResult().getLongitude()));

                            Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                @Override
                                public void onSuccess(@io.reactivex.annotations.NonNull String s) {
                                    edt_address.setText(coordinates);
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    edt_address.setText(coordinates);
                                    txt_address.setText("" + e.getMessage());
                                    txt_address.setVisibility(View.VISIBLE);
                                }
                            });


                        });

            }
        });

        builder.setView(view);
        builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("YES", (dialogInterface, i) -> {
                    if (rdi_cod.isChecked()) {
                        paymentCOD(edt_address.getText().toString(), edt_comment.getText().toString());

                    } else if (rdi_braintree.isChecked()) {
                        address = edt_address.getText().toString();
                        comment = edt_comment.getText().toString();

                        if (!TextUtils.isEmpty(Common.currentToken)) {
                            DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                            startActivityForResult(dropInRequest.getIntent(getContext()), REQUEST_BRAINTREE_CODE);
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {
                                double finalPrice = totalPrice;
                                Order order = new Order();
                                order.setUserId(Common.currentUser.getUid());
                                order.setUserName(Common.currentUser.getName());
                                order.setUserPhone(Common.currentUser.getPhone());
                                order.setShippingAddress(address);
                                order.setComment(comment);

                                if (currentLocation != null) {
                                    order.setLat(currentLocation.getLatitude());
                                    order.setLng(currentLocation.getLongitude());

                                } else {
                                    order.setLng(-0.1f);
                                    order.setLat(-0.1f);
                                }

                                order.setCartItemList(cartItems);
                                order.setTotalPayment(totalPrice);
                                order.setDiscount(0); //implement discount functionality late todo
                                order.setFinalPayment(finalPrice);
                                order.setCod(true);
                                order.setTransactionId("Cash On Delivery");

                                writeOrderToFirebase(order);

                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }), throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
}

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
        .addOnCompleteListener(task -> cartDataSource.cleanCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                        Toast.makeText(getContext(), "Order Placed", Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private String getAddressFromLatLnng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result;
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                result = address.getAddressLine(0);
            } else {
                result = "Address not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;

    }

    private Unbinder unbinder;
    private MyCartAdapter myCartAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        cloudFunctions = RetrofitICloudClient.getInstance().create(ICloudFunctions.class);

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartList().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems.isEmpty() || cartItems == null) {
                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
            } else {
                Log.e("TAG", "onChanged: here");
                recycler_cart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                myCartAdapter = new MyCartAdapter(getContext(), cartItems);
                recycler_cart.setAdapter(myCartAdapter);
            }
        });
        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000)
                .setSmallestDisplacement(10f);
    }

    private void initViews() {
        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = myCartAdapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            myCartAdapter.notifyItemRemoved(pos);
                                            sumAllItemsInCart();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Update FAB
                                            Toast.makeText(getContext(), "DELETE ITEM FROM CART", Toast.LENGTH_SHORT).show();


                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            Toast.makeText(getContext(), "DELETE CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };

        sumAllItemsInCart();
    }

    private void sumAllItemsInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: $").append(aDouble));
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (!e.getMessage().contains("Query returned empty")) {
                            Toast.makeText(getContext(), "SUM CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); //hide home menu already inflate

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            Toast.makeText(getContext(), "CLEANED CART", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "CLEAN CART" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        compositeDisposable.clear();
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
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
                        txt_total_price.setText(new StringBuilder("Total : $")
                                .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "SUM CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BRAINTREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();

                cartDataSource.sumPriceInCart(Common.currentUser.getUid()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {
                                compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid()).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(cartItems -> compositeDisposable.add(cloudFunctions.submitPayment(totalPrice, nonce.getNonce())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(brainTreeTransaction -> {
                                            if (brainTreeTransaction.isSuccess()) {
                                                double finalPrice = totalPrice;
                                                Order order = new Order();
                                                order.setUserId(Common.currentUser.getUid());
                                                order.setUserName(Common.currentUser.getName());
                                                order.setUserPhone(Common.currentUser.getPhone());
                                                order.setShippingAddress(address);
                                                order.setComment(comment);

                                                if (currentLocation != null) {
                                                    order.setLat(currentLocation.getLatitude());
                                                    order.setLng(currentLocation.getLongitude());

                                                } else {
                                                    order.setLng(-0.1f);
                                                    order.setLat(-0.1f);
                                                }

                                                order.setCartItemList(cartItems);
                                                order.setTotalPayment(totalPrice);
                                                order.setDiscount(0); //implement discount functionality late todo
                                                order.setFinalPayment(finalPrice);
                                                order.setCod(false);
                                                order.setTransactionId(brainTreeTransaction.getTransaction().getId());

                                                writeOrderToFirebase(order);
                                            }
                                        }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show())
                                        ), throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        }
    }
}