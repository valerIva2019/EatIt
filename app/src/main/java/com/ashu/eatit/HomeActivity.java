package com.ashu.eatit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.BestDealItemClick;
import com.ashu.eatit.EventBus.CategoryClick;
import com.ashu.eatit.EventBus.CounterCartEvent;
import com.ashu.eatit.EventBus.FoodItemClick;
import com.ashu.eatit.EventBus.HideFABCart;
import com.ashu.eatit.EventBus.MenuInflateEvent;
import com.ashu.eatit.EventBus.MenuItemBack;
import com.ashu.eatit.EventBus.MenuItemEvent;
import com.ashu.eatit.EventBus.PopularCategoryClick;
import com.ashu.eatit.Model.BestDealModel;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.Model.UserModel;
import com.ashu.eatit.Remote.ICloudFunctions;
import com.ashu.eatit.Remote.RetrofitICloudClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;
    private Place placeSelected;
    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
    private ICloudFunctions cloudFunctions;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CartDataSource cartDataSource;
    int menuClickId = -1;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fab)
    CounterFab fab;

    android.app.AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();
        //countCartItem();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        initPlacesClient();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        //countCartItem();

        fab.setOnClickListener(view -> navController.navigate(R.id.nav_cart));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_restaurant, R.id.nav_home, R.id.nav_menu, R.id.nav_food_list, R.id.nav_food_detail, R.id.nav_cart,
                R.id.nav_view_orders)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey, ", Common.currentUser.getName(), txt_user);

        //Hide Fab beacuse restaurant list is shown
        //countCartItem();

        EventBus.getDefault().postSticky(new HideFABCart(true));

    }

    private void initPlacesClient() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_restaurant:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_restaurant);
                break;
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                {
                    navController.navigate(R.id.nav_home);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_menu:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_menu);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_cart);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_view_orders:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_view_orders);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));

                }
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            case R.id.nav_update_info:
                showUpdateInfoDialog();
                break;
        }
        menuClickId = item.getItemId();
        return true;
    }

    private void showUpdateInfoDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Info");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        TextView txt_address_detail = itemView.findViewById(R.id.txt_address_detail);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address_detail.setText(place.getAddress());


            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("PLACES API", "onError: " + status.getStatusMessage());
                Toast.makeText(HomeActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        edt_name.setText(Common.currentUser.getName());
        txt_address_detail.setText(Common.currentUser.getAddress());
        edt_phone.setText(Common.currentUser.getPhone());
        builder.setView(itemView);

        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("UPDATE", (dialogInterface, i) -> {

            if (placeSelected != null) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("name", edt_name.getText().toString());
                updateData.put("address", txt_address_detail.getText().toString());
                updateData.put("lat", placeSelected.getLatLng().latitude);
                updateData.put("lng", placeSelected.getLatLng().longitude);

                FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES)
                        .child(Common.currentUser.getUid())
                        .updateChildren(updateData)
                        .addOnFailureListener(e -> {
                            dialogInterface.dismiss();
                            Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }).addOnSuccessListener(aVoid -> {
                    dialogInterface.dismiss();
                    Toast.makeText(HomeActivity.this, "Updated Info", Toast.LENGTH_LONG).show();
                    Common.currentUser.setName(updateData.get("name").toString());
                    Common.currentUser.setAddress(updateData.get("address").toString());
                    Common.currentUser.setLat(Double.parseDouble(updateData.get("lat").toString()));
                    Common.currentUser.setLng(Double.parseDouble(updateData.get("lng").toString()));

                });


            } else {
                Toast.makeText(this, "Please select your address", Toast.LENGTH_LONG).show();
            }


        });

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to log out ?")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Common.selectedFood = null;
                    Common.categorySelected = null;
                    Common.currentUser = null;
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_list);
            //Toast.makeText(this, "Click to " + event.getCategoryModel().getName(), Toast.LENGTH_LONG).show();

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_detail);
            //Toast.makeText(this, "Click to " + event.getCategoryModel().getName(), Toast.LENGTH_LONG).show();

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
        if (event.isSuccess()) {
            //countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void countCartAgain(CounterCartEvent event) {
        if (event.isSuccess())
            if (Common.restaurantSelected != null)
                countCartItem();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden()) {
            fab.hide();
        } else
            fab.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                    .child(Common.restaurantSelected.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());


                                FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                        .child(Common.restaurantSelected.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());

                                                    }
                                                    navController.navigate(R.id.nav_food_detail);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else
            fab.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                    .child(Common.restaurantSelected.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                                        .child(Common.restaurantSelected.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());

                                                    }
                                                    navController.navigate(R.id.nav_food_detail);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else
            fab.show();
    }

    private void countCartItem() {
        Log.d("TAG", "countCartItem: " + Common.currentUser.getUid());
        cartDataSource.countItemInCart(Common.currentUser.getUid(), Common.restaurantSelected.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                        fab.setCount(integer);

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (!e.getMessage().contains("Query returned empty")) {
                            Toast.makeText(HomeActivity.this, "CART ERROR" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            fab.setCount(0);
                        }

                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event) {
        menuClickId = -1;
        //navController.popBackStack(R.id.nav_home, true);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRestaurantClick(MenuItemEvent event) {
        cloudFunctions = RetrofitICloudClient.getInstance(event.getRestaurantModel().getPaymentUrl()).create(ICloudFunctions.class);
        dialog.show();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildToken(Common.authorizeKey));
        compositeDisposable.add(cloudFunctions.getToken(headers).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(brainTreeToken -> {
                    Common.currentToken = brainTreeToken.getToken();
                    Bundle bundle = new Bundle();
                    bundle.putString("restaurant", event.getRestaurantModel().getUid());
                    navController.navigate(R.id.nav_home, bundle);
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.restaurant_detail_menu);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                    EventBus.getDefault().postSticky(new HideFABCart(false));
                    countCartItem();
                    dialog.dismiss();

                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(HomeActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onInflateMenu(MenuInflateEvent event) {
        navigationView.getMenu().clear();
        if (event.isShowDetail())
            navigationView.inflateMenu(R.menu.restaurant_detail_menu);
        else
            navigationView.inflateMenu(R.menu.activity_main_drawer);
    }
}