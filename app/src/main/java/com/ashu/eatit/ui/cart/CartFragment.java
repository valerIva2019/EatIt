package com.ashu.eatit.ui.cart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.Adapter.MyCartAdapter;
import com.ashu.eatit.Callback.ILoadTimeFromFirebaseListener;
import com.ashu.eatit.Callback.ISearchCategoryCallbackListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Common.MySwiperHelper;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.CounterCartEvent;
import com.ashu.eatit.EventBus.HideFABCart;
import com.ashu.eatit.EventBus.MenuItemBack;
import com.ashu.eatit.EventBus.UpdateItemInCart;
import com.ashu.eatit.Model.AddonModel;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.DiscountModel;
import com.ashu.eatit.Model.FCMSendData;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.Model.OrderModel;
import com.ashu.eatit.Model.RestaurantLocationModel;
import com.ashu.eatit.Model.SizeModel;
import com.ashu.eatit.R;
import com.ashu.eatit.Remote.ICloudFunctions;
import com.ashu.eatit.Remote.IFCMService;
import com.ashu.eatit.Remote.RetrofitFCMClient;
import com.ashu.eatit.Remote.RetrofitICloudClient;
import com.ashu.eatit.ScanQRActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener, TextWatcher {

    private static final int SCAN_QR_PERMISSION = 7171;
    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon, chip_group_user_selected_addon;
    private EditText edt_search;

    ISearchCategoryCallbackListener iSearchCategoryCallbackListener;
    private Place placeSelected;
    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

    private static final int REQUEST_BRAINTREE_CODE = 7777;
    private CartViewModel cartViewModel;
    String address, comment;
    ICloudFunctions cloudFunctions;

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    IFCMService ifcmService;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    ILoadTimeFromFirebaseListener listener;

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
    @BindView(R.id.edt_discount_code)
    EditText edt_discount_code;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.apply_discount)
    LinearLayout apply_discount;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.discount_arrow)
    ImageView discount_arrow;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.expandable_layout)
    ExpandableLayout expandable_layout;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_scan)
    void onScanQRCode() {
        startActivityForResult(new Intent(requireContext(), ScanQRActivity.class), SCAN_QR_PERMISSION);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.apply_discount)
    void onShowClick() {
        if (expandable_layout.isExpanded()) {
            discount_arrow.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_arrow_drop_up_24));
        } else {
            discount_arrow.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_arrow_drop_down_24));
        }
        expandable_layout.toggle();
    }



    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_check)
    void onApplyDiscount() {
        if (!TextUtils.isEmpty(edt_discount_code.getText().toString())) {
            android.app.AlertDialog alertDialog = new SpotsDialog.Builder().setContext(getContext())
                    .setMessage("Applying discount ...")
                    .setCancelable(false)
                    .build();

            alertDialog.show();

            final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
            final DatabaseReference discountRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                    .child(Common.restaurantSelected.getUid())
                    .child(Common.DISCOUNT);

            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long offset = snapshot.getValue(Long.class);
                    long estimateServerTimeMs = System.currentTimeMillis() + offset;

                    discountRef.child(edt_discount_code.getText().toString().toLowerCase())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        DiscountModel discountModel = snapshot.getValue(DiscountModel.class);
                                        discountModel.setKey(snapshot.getKey());

                                        if (discountModel.getUntilDate() < estimateServerTimeMs) {
                                            alertDialog.dismiss();
                                            listener.onLoadTimeFailed("Discount expired");
                                        } else {
                                            Common.discountApply = discountModel;
                                            sumAllItemsInCart();
                                            alertDialog.dismiss();
                                        }
                                    }
                                    else {
                                        alertDialog.dismiss();
                                        listener.onLoadTimeFailed("Discount not valid");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    alertDialog.dismiss();
                                    listener.onLoadTimeFailed(error.getMessage());
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    alertDialog.dismiss();
                    listener.onLoadTimeFailed(error.getMessage());

                }
            });
        }
    }

    ;


    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step!!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_comment = view.findViewById(R.id.edt_comment);
        TextView txt_address = view.findViewById(R.id.txt_address_detail);

        RadioButton rdi_home = view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_this_address = view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = view.findViewById(R.id.rdi_braintree);

        places_fragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address.setText(place.getAddress());


            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("PLACES API", "onError: " + status.getStatusMessage());
                Toast.makeText(getContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        txt_address.setText(Common.currentUser.getAddress());
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                txt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.VISIBLE);
                places_fragment.setHint(Common.currentUser.getAddress());

            }
        });
        rdi_other_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                txt_address.setVisibility(View.VISIBLE);

            }
        });
        rdi_ship_this_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);
                                    places_fragment.setHint(s);

                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
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
                    dialogInterface.dismiss();
                    if (rdi_cod.isChecked()) {
                        paymentCOD(txt_address.getText().toString(), edt_comment.getText().toString());

                    } else if (rdi_braintree.isChecked()) {
                        address = txt_address.getText().toString();
                        comment = edt_comment.getText().toString();

                        if (!TextUtils.isEmpty(Common.currentToken)) {
                            DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                            startActivityForResult(dropInRequest.getIntent(getContext()), REQUEST_BRAINTREE_CODE);
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> {
            if (places_fragment != null)
                getActivity().getSupportFragmentManager()
                        .beginTransaction().remove(places_fragment)
                        .commit();
        });
        dialog.show();


    }

    private void paymentCOD(String address, String comment) {

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.restaurantSelected.getUid())
                .child(Common.LOCATION_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            RestaurantLocationModel location = snapshot.getValue(RestaurantLocationModel.class);
                            applyShippingCostByLocation(location);

                        } else {
                            applyShippingCostByLocation(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        }

    private void applyShippingCostByLocation(RestaurantLocationModel location) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> cartDataSource.sumPriceInCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {
                                double finalPrice = totalPrice;
                                OrderModel orderModel = new OrderModel();
                                orderModel.setUserId(Common.currentUser.getUid());
                                orderModel.setUserName(Common.currentUser.getName());
                                orderModel.setUserPhone(Common.currentUser.getPhone());
                                orderModel.setShippingAddress(address);
                                orderModel.setComment(comment);

                                if (currentLocation != null) {
                                    orderModel.setLat(currentLocation.getLatitude());
                                    orderModel.setLng(currentLocation.getLongitude());

                                    if (location != null) {
                                        Location orderLocation = new Location("");
                                        orderLocation.setLatitude(currentLocation.getLatitude());
                                        orderLocation.setLongitude(currentLocation.getLongitude());

                                        Location restaurantLocation = new Location("");
                                        restaurantLocation.setLatitude(location.getLat());
                                        restaurantLocation.setLongitude(location.getLng());

                                        float distance = orderLocation.distanceTo(restaurantLocation)/1000; //in km
                                        if (distance * Common.SHIPPING_COST_PER_KM > Common.MAX_SHIPPING_COST) {
                                            orderModel.setShippingCost(Common.MAX_SHIPPING_COST);
                                        } else {
                                           orderModel.setShippingCost(distance * Common.SHIPPING_COST_PER_KM);
                                        }

                                    } else {
                                        orderModel.setShippingCost(0);
                                    }

                                } else {
                                    orderModel.setLng(-0.1f);
                                    orderModel.setLat(-0.1f);

                                    orderModel.setShippingCost(Common.MAX_SHIPPING_COST);
                                }

                                orderModel.setCartItemList(cartItems);
                                orderModel.setTotalPayment(totalPrice);
                                if (Common.discountApply != null) {
                                    orderModel.setDiscount(Common.discountApply.getPercent());
                                } else
                                    orderModel.setDiscount(0);
                                orderModel.setFinalPayment(finalPrice);
                                orderModel.setCod(true);
                                orderModel.setTransactionId("Cash On Delivery");

                                syncLocalTimeWithGlobalTime(orderModel);

                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                if (!e.getMessage().contains("Query returned empty result set")) {
                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }), throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));

    }

    private void syncLocalTimeWithGlobalTime(OrderModel orderModel) {
       AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).setTitle("Shipping Cost")
               .setMessage("We will take " + orderModel.getFinalPayment() + Math.round(orderModel.getShippingCost()) + "for shipping")
               .setNegativeButton("NO", (dialog, which) -> dialog.dismiss()).setPositiveButton("YES", (dialog, which) -> {
                   dialog.dismiss();
                   final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                   offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           long offset = snapshot.getValue(Long.class);
                           long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                           SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                           Date resultDate = new Date(estimatedServerTimeMs);
                           Log.d("TEST_DATE", "" + sdf.format(resultDate));

                           listener.onLoadTimeSuccess(orderModel, estimatedServerTimeMs);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {
                           listener.onLoadTimeFailed(error.getMessage());
                       }
                   });
               }).create();
       alertDialog.show();
    }

    private void writeOrderToFirebase(OrderModel orderModel) {
        FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(Common.restaurantSelected.getUid())
                .child(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(orderModel)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> cartDataSource.cleanCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                Map<String, String> notiData = new HashMap<>();
                                notiData.put(Common.NOT1_TITLE, "New Order");
                                notiData.put(Common.NOT1_CONTENT, "You have new order from " + Common.currentUser.getPhone());

                                FCMSendData sendData = new FCMSendData(Common.createTopicOrder(), notiData);


                                compositeDisposable.add(ifcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(fcmResponse -> {
                                            Toast.makeText(getContext(), "Order Placed", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }, throwable -> {
                                            Toast.makeText(getContext(), "Order was placed but failed to send notification", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }));


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

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        cloudFunctions = RetrofitICloudClient.getInstance(Common.restaurantSelected.getPaymentUrl()).create(ICloudFunctions.class);
        listener = this;

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

        initPlacesClient();
        iSearchCategoryCallbackListener = this;
        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);
        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddOn();
            calculateTotalPrice();
        });

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
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#5D4037"),
                        pos -> {
                            CartItem cartItem = myCartAdapter.getItemAtPosition(pos);
                            FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                    .child(Common.restaurantSelected.getUid())
                                    .child(Common.CATEGORY_REF)
                                    .child(cartItem.getCategoryId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                CategoryModel categoryModel = snapshot.getValue(CategoryModel.class);
                                                iSearchCategoryCallbackListener.onSearchCategoryFound(categoryModel, cartItem);
                                            } else {
                                                iSearchCategoryCallbackListener.onSearchCategoryNotFound("Food not found");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            iSearchCategoryCallbackListener.onSearchCategoryNotFound(error.getMessage());

                                        }
                                    });

                        }));
            }
        };

        sumAllItemsInCart();
    }

    private void displayUserSelectedAddOn() {
        if (Common.selectedFood.getUserSelectedAddon() != null &&
                Common.selectedFood.getUserSelectedAddon().size() > 0) {
            chip_group_user_selected_addon.removeAllViews(); //clear all views already added
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) //add all addon to the list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }

        } else
            chip_group_user_selected_addon.removeAllViews();

    }

    private void initPlacesClient() {
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());
    }

    private void sumAllItemsInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double aDouble) {
                        if (Common.discountApply != null) {
                            aDouble = aDouble - (aDouble * Common.discountApply.getPercent() / 100);
                            txt_total_price.setText(new StringBuilder("Total: $").append(aDouble)
                            .append("(-")
                            .append(Common.discountApply.getPercent())
                            .append("%)"));
                        } else {
                            txt_total_price.setText(new StringBuilder("Total: $").append(aDouble));
                        }
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
            cartDataSource.cleanCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
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
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));
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
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
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
                        if (!e.getMessage().contains("Query returned empty result set")) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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

                cartDataSource.sumPriceInCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.RESTAURANT_REF)
                                        .child(Common.restaurantSelected.getUid())
                                        .child(Common.LOCATION_REF)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    RestaurantLocationModel location = snapshot.getValue(RestaurantLocationModel.class);
                                                    applyShippingCostForPaymentByLocation(location, totalPrice, nonce);

                                                } else {
                                                    applyShippingCostForPaymentByLocation(null, totalPrice, nonce);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid()).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(cartItems -> {
                                            Map<String, String> headers = new HashMap<>();
                                            headers.put("Authorization", Common.buildToken(Common.authorizeKey));
                                            compositeDisposable.add(cloudFunctions.submitPayment(headers, totalPrice, nonce.getNonce())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(brainTreeTransaction -> {
                                                        if (brainTreeTransaction.isSuccess()) {
                                                            double finalPrice = totalPrice;
                                                            OrderModel orderModel = new OrderModel();
                                                            orderModel.setUserId(Common.currentUser.getUid());
                                                            orderModel.setUserName(Common.currentUser.getName());
                                                            orderModel.setUserPhone(Common.currentUser.getPhone());
                                                            orderModel.setShippingAddress(address);
                                                            orderModel.setComment(comment);

                                                            if (currentLocation != null) {
                                                                orderModel.setLat(currentLocation.getLatitude());
                                                                orderModel.setLng(currentLocation.getLongitude());

                                                            } else {
                                                                orderModel.setLng(-0.1f);
                                                                orderModel.setLat(-0.1f);
                                                            }

                                                            orderModel.setCartItemList(cartItems);
                                                            orderModel.setTotalPayment(totalPrice);
                                                            if (Common.discountApply != null) {
                                                                orderModel.setDiscount(Common.discountApply.getPercent());
                                                            } else
                                                                orderModel.setDiscount(0);
                                                            orderModel.setFinalPayment(finalPrice);
                                                            orderModel.setCod(false);
                                                            orderModel.setTransactionId(brainTreeTransaction.getTransaction().getId());

                                                            //writeOrderToFirebase(orderModel);
                                                            syncLocalTimeWithGlobalTime(orderModel);
                                                        }

                                                    }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                                        }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                if (!e.getMessage().contains("Query returned empty result set")) {
                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else if (requestCode == SCAN_QR_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                edt_discount_code.setText(data.getStringExtra(Common.QR_CODE_TAG).toLowerCase());
            }
        }
    }

    private void applyShippingCostForPaymentByLocation(RestaurantLocationModel location, Double totalPrice, PaymentMethodNonce nonce) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Common.buildToken(Common.authorizeKey));
                    compositeDisposable.add(cloudFunctions.submitPayment(headers, totalPrice, nonce.getNonce())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(brainTreeTransaction -> {
                                if (brainTreeTransaction.isSuccess()) {
                                    double finalPrice = totalPrice;
                                    OrderModel orderModel = new OrderModel();
                                    orderModel.setUserId(Common.currentUser.getUid());
                                    orderModel.setUserName(Common.currentUser.getName());
                                    orderModel.setUserPhone(Common.currentUser.getPhone());
                                    orderModel.setShippingAddress(address);
                                    orderModel.setComment(comment);

                                    if (currentLocation != null) {
                                        orderModel.setLat(currentLocation.getLatitude());
                                        orderModel.setLng(currentLocation.getLongitude());

                                        if (location != null) {
                                            Location orderLocation = new Location("");
                                            orderLocation.setLatitude(currentLocation.getLatitude());
                                            orderLocation.setLongitude(currentLocation.getLongitude());

                                            Location restaurantLocation = new Location("");
                                            restaurantLocation.setLatitude(location.getLat());
                                            restaurantLocation.setLongitude(location.getLng());

                                            float distance = orderLocation.distanceTo(restaurantLocation)/1000; //in km
                                            if (distance * Common.SHIPPING_COST_PER_KM > Common.MAX_SHIPPING_COST) {
                                                orderModel.setShippingCost(Common.MAX_SHIPPING_COST);
                                            } else {
                                                orderModel.setShippingCost(distance * Common.SHIPPING_COST_PER_KM);
                                            }

                                        } else {
                                            orderModel.setShippingCost(0);
                                        }

                                    } else {
                                        orderModel.setLng(-0.1f);
                                        orderModel.setLat(-0.1f);

                                        orderModel.setShippingCost(Common.MAX_SHIPPING_COST);

                                    }

                                    orderModel.setCartItemList(cartItems);
                                    orderModel.setTotalPayment(totalPrice);
                                    if (Common.discountApply != null) {
                                        orderModel.setDiscount(Common.discountApply.getPercent());
                                    } else
                                        orderModel.setDiscount(0);
                                    orderModel.setFinalPayment(finalPrice);
                                    orderModel.setCod(false);
                                    orderModel.setTransactionId(brainTreeTransaction.getTransaction().getId());

                                    //writeOrderToFirebase(orderModel);
                                    syncLocalTimeWithGlobalTime(orderModel);
                                }

                            }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));

    }

    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs) {
        orderModel.setCreateDate(estimateTimeInMs);
        orderModel.setOrderStatus(0);
        writeOrderToFirebase(orderModel);
    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {

    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }


    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem) {
        FoodModel foodModel = Common.findFoodInListById(categoryModel, cartItem.getFoodId());
        if (foodModel != null) {
            showUpdateDialog(cartItem, foodModel);
        } else {
            Toast.makeText(getContext(), "Food Id not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(CartItem cartItem, FoodModel foodModel) {
        Common.selectedFood = foodModel;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart, null);
        builder.setView(itemView);

        Button btn_ok = itemView.findViewById(R.id.btn_ok);
        Button btn_cancel = itemView.findViewById(R.id.btn_cancel);

        RadioGroup rdi_group_size = itemView.findViewById(R.id.rdi_group_size);
        chip_group_user_selected_addon = itemView.findViewById(R.id.chip_group_user_selected_addon);
        ImageView img_add_on = itemView.findViewById(R.id.img_add_addon);
        img_add_on.setOnClickListener(view -> {
            if (foodModel != null) {
                displayAddonList();
                addonBottomSheetDialog.show();
            }
        });

        if (foodModel.getSize() != null) {
            for (SizeModel sizeModel : foodModel.getSize()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice();
                });

                ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);

            }

            if (rdi_group_size.getChildCount() > 0) {
                RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
                radioButton.setChecked(true);
            }
        }

        //addon
        displayAlreadySelectedAddon(chip_group_user_selected_addon, cartItem);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_ok.setOnClickListener(view -> cartDataSource.deleteCartItem(cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                        if (Common.selectedFood.getUserSelectedAddon() != null)
                            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
                        else
                            cartItem.setFoodAddon("Default");
                        if (Common.selectedFood.getUserSelectedSize() != null)
                            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
                        else
                            cartItem.setFoodSize("Default");

                        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),
                                Common.selectedFood.getUserSelectedAddon()));

                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                                    calculateTotalPrice();
                                    dialog.dismiss();
                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                }, throwable -> Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void displayAlreadySelectedAddon(ChipGroup chip_group_user_selected_addon, CartItem cartItem) {
        if (cartItem.getFoodAddon() != null &&
                !cartItem.getFoodAddon().equals("Default")) {
            List<AddonModel> addonModels = new Gson().fromJson(cartItem.getFoodAddon(), new TypeToken<List<AddonModel>>() {
            }.getType());
            Common.selectedFood.setUserSelectedAddon(addonModels);
            chip_group_user_selected_addon.removeAllViews();


            for (AddonModel addonModel : addonModels) //add all addon to the list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }

        }
    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon() != null && Common.selectedFood.getAddon().size() > 0) {
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            for (AddonModel addonModel : Common.selectedFood.getAddon()) {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }

        }
    }

    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for (AddonModel addonModel : Common.selectedFood.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
