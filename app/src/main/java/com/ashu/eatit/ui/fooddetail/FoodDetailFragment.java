package com.ashu.eatit.ui.fooddetail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.ashu.eatit.MainActivity;
import com.ashu.eatit.Model.CommentModel;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.Model.UserModel;
import com.ashu.eatit.R;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodDetailFragment extends Fragment {

    private FoodDetailViewModel foodDetailViewModel;
    private Unbinder unbinder;
    private AlertDialog waitingDialog;

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
    @OnClick(R.id.btn_rating)
    void onRatingButtonClick() {
        showDialogRating();
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
            commentModel.setCommentTimeStamp(serverTimeStamp);

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
        waitingDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
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

    }
}