package com.apps.adrcotfas.goodtime.Statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesAdapter extends RecyclerView.Adapter<AllEntriesViewHolder> {
    public List<Session> mEntries = new ArrayList<>();
    public List<Long> mSelectedEntries = new ArrayList<>();

    AllEntriesAdapter() {
        // this and the override of getItemId are to avoid clipping in the view
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public AllEntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View statusContainer = inflater.inflate(R.layout.statistics_all_entries_row, parent, false);
        return new AllEntriesViewHolder(statusContainer);
    }

    @Override
    public void onBindViewHolder(@NonNull AllEntriesViewHolder holder, int position) {
        Session session = mEntries.get(position);
        holder.bind(session);
        holder.rowOverlay.setVisibility(
                mSelectedEntries.contains(mEntries.get(position).endTime) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    @Override
    public long getItemId(int position) {
        // TODO: return id
        Session session = mEntries.get(position);
        return session.endTime;
    }

    public void setData(List<Session> newSessions) {
        if (mEntries != null) {
            PostDiffCallback postDiffCallback = new PostDiffCallback(mEntries, newSessions);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(postDiffCallback);

            mEntries.clear();
            mEntries.addAll(newSessions);
            diffResult.dispatchUpdatesTo(this);
        } else {
            // first initialization
            mEntries = newSessions;
        }
    }

    public void setSelectedItems(List<Long> selectedItems) {
        this.mSelectedEntries = selectedItems;
        notifyDataSetChanged();
    }

    class PostDiffCallback extends DiffUtil.Callback {

        private final List<Session> oldSessions, newSessions;

        public PostDiffCallback(List<Session> oldSessions, List<Session> newSessions) {
            this.oldSessions = oldSessions;
            this.newSessions = newSessions;
        }

        @Override
        public int getOldListSize() {
            return oldSessions.size();
        }

        @Override
        public int getNewListSize() {
            return newSessions.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldSessions.get(oldItemPosition).endTime == newSessions.get(newItemPosition).endTime;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldSessions.get(oldItemPosition).equals(newSessions.get(newItemPosition));
        }
    }
}