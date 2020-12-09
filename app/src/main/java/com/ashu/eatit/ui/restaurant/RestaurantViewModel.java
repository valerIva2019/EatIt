package com.ashu.eatit.ui.restaurant;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ashu.eatit.Callback.ICategoryCallbackListener;
import com.ashu.eatit.Callback.IRecyclerClickListener;
import com.ashu.eatit.Callback.IRestaurantCallbackListener;
import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.RestaurantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantViewModel extends ViewModel implements IRestaurantCallbackListener {
    private MutableLiveData<List<RestaurantModel>> restaurantListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IRestaurantCallbackListener listener;

    public RestaurantViewModel() {
        listener = this;
    }


    public MutableLiveData<List<RestaurantModel>> getRestaurantListMutable() {
        if (restaurantListMutable == null) {
            restaurantListMutable = new MutableLiveData<>();
            loadRestaurantsFromFirebase();
        }
        return restaurantListMutable;
    }

    private void loadRestaurantsFromFirebase() {
        List<RestaurantModel> tempList = new ArrayList<>();
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                        RestaurantModel model = restaurantSnapshot.getValue(RestaurantModel.class);
                        model.setUid(restaurantSnapshot.getKey());
                        tempList.add(model);
                    }
                    if (tempList.size() > 0) {
                        listener.onRestaurantLoadSuccess(tempList);
                    } else
                        listener.onRestaurantLoadFailed("Restaurant list empty");
                } else {
                    listener.onRestaurantLoadFailed("Restaurant list doesn't exist");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onRestaurantLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList) {
        restaurantListMutable.setValue(restaurantModelList);
    }

    @Override
    public void onRestaurantLoadFailed(String message) {
        messageError.setValue(message);
    }
}