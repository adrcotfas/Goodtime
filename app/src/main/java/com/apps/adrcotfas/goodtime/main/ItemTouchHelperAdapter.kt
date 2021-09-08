package com.apps.adrcotfas.goodtime.main

interface ItemTouchHelperAdapter {
    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and not at the end of a "drop" event.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then end position of the moved item.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int)

    /**
     * Called when an item has been dragged at the end of a "drop" event.
     */
    fun onClearView()
}