package com.apps.adrcotfas.goodtime.Main;

import androidx.recyclerview.widget.ItemTouchHelper;

public interface ItemTouchHelperViewHolder {

    /**
     * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
     * Implementations should update the item view to indicate it's active state.
     */
    void onItemSelected();
}
