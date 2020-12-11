package com.ashu.eatit.Callback;

import com.ashu.eatit.Database.CartItem;
import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
