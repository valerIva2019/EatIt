package com.ashu.eatit.Database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = CartItem.class, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {
    public abstract CartDAO cartDAO();
    private static CartDatabase instance;

    public static CartDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, CartDatabase.class, "EatItDB1").build();
        }
        return instance;
    }

}
