package com.ashu.eatit.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cart")
public class CartItem {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "foodId")
    private String foodId;

    @ColumnInfo(name = "foodName")
    private String foodName;

    @ColumnInfo(name = "foodImg")
    private String foodImg;

    @ColumnInfo(name = "foodPrice")
    private Double foodPrice;

    @ColumnInfo(name = "foodQuantity")
    private int foodQuantity;

    @ColumnInfo(name = "userPhone")
    private String userPhone;

    @ColumnInfo(name = "foodExtraPrice")
    private String foodExtraPrice;

    @ColumnInfo(name = "foodAddon")
    private String foodAddon;

    @ColumnInfo(name = "foodSize")
    private String foodSize;

    @ColumnInfo(name = "uid")
    private String uid;


    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImg() {
        return foodImg;
    }

    public void setFoodImg(String foodImg) {
        this.foodImg = foodImg;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public int getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodExtraPrice() {
        return foodExtraPrice;
    }

    public void setFoodExtraPrice(String foodExtraPrice) {
        this.foodExtraPrice = foodExtraPrice;
    }

    public String getFoodAddon() {
        return foodAddon;
    }

    public void setFoodAddon(String foodAddon) {
        this.foodAddon = foodAddon;
    }

    public String getFoodSize() {
        return foodSize;
    }

    public void setFoodSize(String foodSize) {
        this.foodSize = foodSize;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
