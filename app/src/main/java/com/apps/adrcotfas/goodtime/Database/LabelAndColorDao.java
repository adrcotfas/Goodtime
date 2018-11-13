package com.apps.adrcotfas.goodtime.Database;

import com.apps.adrcotfas.goodtime.LabelAndColor;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface LabelAndColorDao {

    @Insert(onConflict = IGNORE)
    void addLabel(LabelAndColor session);

    @Query("select * from LabelAndColor")
    LiveData<List<LabelAndColor>> getLabels();

    @Query("select color from LabelAndColor where label = :label")
    LiveData<Integer> getColor(String label);

    @Query("update LabelAndColor SET label = :label WHERE label = :id")
    void editLabelName(String id, String label);

    @Query("update LabelAndColor SET color = :color WHERE label = :id")
    void editLabelColor(String id, int color);

    @Query("delete from LabelAndColor where label = :label")
    void deleteLabel(String label);

    @Query("update LabelAndColor SET label = :newLabel, color = :color WHERE label = :label")
    void updateLabel(String label, String newLabel, int color);
}
