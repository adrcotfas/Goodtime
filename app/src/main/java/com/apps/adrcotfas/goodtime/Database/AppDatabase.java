package com.apps.adrcotfas.goodtime.Database;

import android.content.Context;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Session;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Session.class, LabelAndColor.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    private static final Object LOCK = new Object();
    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
            synchronized (LOCK) {
                if (INSTANCE == null || !INSTANCE.isOpen()) {
                    INSTANCE =  Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "goodtime-db").build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        closeInstance();
        INSTANCE = null;
    }

    public static void closeInstance() {
        if(INSTANCE.isOpen()) {
            INSTANCE.getOpenHelper().close();
        }
    }

    public abstract SessionDao sessionModel();

    public abstract LabelAndColorDao labelAndColor();
}
