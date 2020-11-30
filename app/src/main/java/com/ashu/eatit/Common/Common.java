package com.ashu.eatit.Common;

import com.ashu.eatit.Model.CategoryModel;
import com.ashu.eatit.Model.FoodModel;
import com.ashu.eatit.Model.UserModel;

public class Common {
    public static final String USER_REFERENCES = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEALS_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static UserModel currentUser;

    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
}
