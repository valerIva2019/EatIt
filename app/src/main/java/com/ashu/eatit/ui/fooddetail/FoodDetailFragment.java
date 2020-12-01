package com.ashu.eatit.ui.fooddetail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andremion.counterfab.CounterFab;
import com.ashu.eatit.Adapter.MyFoodListAdapter;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Database.CartDataSource;
import com.ashu.eatit.Database.CartDatabase;
import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Database.LocalCartDataSource;
import com.ashu.eatit.EventBus.CounterCartEvent;
import com.ashu.eatit.MainActivity;
import com.ashu.eatit.Model.AddonModel;
import com.ashu.eatit.Model.CommentModel;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.Model.SizeModel;
import com.ashu.eatit.Model.UserModel;
import com.ashu.eatit.R;
import com.ashu.eatit.ui.comments.CommentFragment;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailViewModel foodDetailViewModel;
    private Unbinder unbinder;
    private AlertDialog waitingDialog;
    private BottomSheetDialog addOnBottomSheetDialog;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    ChipGroup chip_group_addon;
    EditText edt_search;

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
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.radio_group_size)
    RadioGroup radio_group_size;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_add_addon)
    ImageView img_add_addon;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_rating)
    void onRatingButtonClick() {
        showDialogRating();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnShowComment)
    void onShowButtonClicked() {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment ");
    }
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_add_addon)
    void onAddOnClick() {
       if (Common.selectedFood.getAddon() != null) {
           displayAddOnList(); //show all addons
           addOnBottomSheetDialog.show();
       }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnCart)
    void onCartItemAdd() {
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());

        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImg(Common.selectedFood.getImage());
        cartItem.setFoodQuantity(Integer.parseInt(numberButton.getNumber()));
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon()));

        if (Common.selectedFood.getUserSelectedAddon() != null)
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        else
            cartItem.setFoodAddon("Default");
        if (Common.selectedFood.getUserSelectedSize() != null)
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        else
            cartItem.setFoodSize("Default");

        Log.e("ADD CART", "onError: " + cartItem.getFoodSize() );
        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull CartItem cartItemFromDB) {
                        if (cartItemFromDB.equals(cartItem)) {
                            //already in database, just update
                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            Toast.makeText(getContext(), "Update Cart Success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                        else {
                            //Item not available, add new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                    }, throwable -> Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            //default, if cart is empty
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                    }, throwable -> {
                                        Log.e("ADD CART", "onError: " + throwable.getMessage());
                                        Log.e("ADD CART", "onError: " + cartItem.getFoodSize() );
                                        Toast.makeText(getContext(), "[ADD CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        } else {
                            Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void displayAddOnList() {
        if (Common.selectedFood.getAddon().size() > 0) {
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            for (AddonModel addonModel:Common.selectedFood.getAddon()) {

                    @SuppressLint("InflateParams") Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
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

    private void showDialogRating() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);

        RatingBar ratingBar = (RatingBar)itemView.findViewById(R.id.rating_bar);
        EditText edt_comment = (EditText) itemView.findViewById(R.id.edt_comment);

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            CommentModel commentModel = new CommentModel();
            commentModel.setName(Common.currentUser.getName());
            commentModel.setUid(Common.currentUser.getUid());
            commentModel.setComment(edt_comment.getText().toString());
            commentModel.setRatingValue(ratingBar.getRating());
            Map<String , Object> serverTimeStamp = new HashMap<>();
            serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
            commentModel.setServerTimeStamp(serverTimeStamp);

            foodDetailViewModel.setCommentModel(commentModel);

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                new ViewModelProvider(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        foodDetailViewModel.getMutableLiveDataFood().observe(getViewLifecycleOwner(), this::displayInfo);

        foodDetailViewModel.getMutableLiveDataComment().observe(getViewLifecycleOwner(), this::submitRatingToFirebase);
        return root;
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        waitingDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        addOnBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_add_on_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = (ChipGroup) layout_add_on_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText) layout_add_on_display.findViewById(R.id.edt_search);
        addOnBottomSheetDialog.setContentView(layout_add_on_display);

        addOnBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddOn();
            calculateTotalPrice();
        });
    }

    private void displayUserSelectedAddOn() {
        if (Common.selectedFood.getUserSelectedAddon() != null &&
        Common.selectedFood.getUserSelectedAddon().size() > 0) {
            chip_group_user_selected_addon.removeAllViews(); //clear all views already added
            for(AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) //add all addon to the list
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
        else if (Common.selectedFood.getUserSelectedAddon().size() == 0)
            chip_group_user_selected_addon.removeAllViews();

    }

    private void submitRatingToFirebase(CommentModel commentModel) {
        //submit the comment ref
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //update rating
                        addRatingToFood(commentModel.getRatingValue());
                    }
                    waitingDialog.dismiss();
                });

    }

    private void addRatingToFood(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("foods")
                .child(Common.selectedFood.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            FoodModel foodModel = snapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());

                            //rating
                            if (foodModel.getRatingValue() == null)
                                foodModel.setRatingValue(0d);
                            if (foodModel.getRatingCount() == null)
                                foodModel.setRatingCount(0L);

                            double sumRating = foodModel.getRatingValue() + ratingValue;
                            long ratingCount = foodModel.getRatingCount() + 1;
                            double result = sumRating / ratingCount;

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", result);
                            updateData.put("ratingCount", ratingCount);

                            //update data in variables
                            foodModel.setRatingValue(result);
                            foodModel.setRatingCount(ratingCount);

                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(task -> {
                                        waitingDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Thank You!!", Toast.LENGTH_SHORT).show();
                                            Common.selectedFood = foodModel;
                                            foodDetailViewModel.setFoodModel(foodModel); //call refresh
                                        }

                                    });

                        }
                        else
                            waitingDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() != null)
            ratingBar.setRating(foodModel.getRatingValue().floatValue());

        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());

        //Size
        for (SizeModel sizeModel :Common.selectedFood.getSize()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice(); //update price
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);

            radioButton.setLayoutParams(layoutParams);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            radio_group_size.addView(radioButton);

        }

        if (radio_group_size.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) radio_group_size.getChildAt(0);
            radioButton.setChecked(true);
        }
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice;

        //AddOn
        if (Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0) {
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon())
                totalPrice += Double.parseDouble(addonModel.getPrice().toString());
        }



        //Size
        totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice * 100.0 / 100.0);

        food_price.setText("" + Common.formatPrice(displayPrice));

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for (AddonModel addonModel:Common.selectedFood.getAddon()) {
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

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();

    }
}