package com.sensustech.mytvcast.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.sensustech.mytvcast.db.dao.ChannelDao;
import com.sensustech.mytvcast.db.entities.ChannelModel;

import java.util.concurrent.Executors;

@Database(entities = {ChannelModel.class},
        version = 1,
        exportSchema = false)
public abstract class ChannelDatabase extends RoomDatabase {

    private static ChannelDatabase instance;
    public abstract ChannelDao getChannelDao();

    public static synchronized ChannelDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ChannelDatabase.class,
                            "app_database")
//                    .addMigrations(MIGRATION_1_2)
                    //.fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
//                            sQLiteDatabase = db;

                            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                                @Override
                                public void run() { }
                            });
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                        }
                    })
                    .build();
        }
        return instance;
    }
}
