package com.apps.adrcotfas.goodtime.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.adrcotfas.goodtime.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileSelectAdapter extends RecyclerView.Adapter<ProfileSelectAdapter.ViewHolder>{

    public interface OnProfileSelectedListener{
        void onDelete(int position);
        void onSelect(int position);
    }

    private LayoutInflater inflater;
    private WeakReference<Context> mContext;
    private List<CharSequence> mProfiles;
    private int mClickedDialogEntryIndex;
    private OnProfileSelectedListener mCallback;

    public ProfileSelectAdapter(Context context, CharSequence[] profiles, int selectedIndex, OnProfileSelectedListener callback) {
        inflater = LayoutInflater.from(context);
        mContext = new WeakReference<>(context);
        mProfiles = new ArrayList<>(Arrays.asList(profiles));
        mClickedDialogEntryIndex = selectedIndex;
        mCallback = callback;
    }

    @NonNull
    @Override
    public ProfileSelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dialog_select_profile_row, parent, false);
        return new ProfileSelectAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String profile = mProfiles.get(position).toString();

        // don't delete the predefined profiles
        boolean showDeleteButton =
                !profile.equals(mContext.get().getString(R.string.pref_profile_5217)) &&
                !profile.equals(mContext.get().getString(R.string.pref_profile_default));
        holder.text.setText(profile);

        if (mClickedDialogEntryIndex != -1) {
            holder.text.setChecked(mProfiles.get(mClickedDialogEntryIndex).equals(profile));
        }
        holder.deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);
        holder.deleteButton.setOnClickListener(v -> {
            mCallback.onDelete(position);
            mProfiles.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
            if (position == mClickedDialogEntryIndex) {
                mClickedDialogEntryIndex = 0;
                // 1 because of the predefined profiles (25/5 is 0, 52/17 is 1)
            } else if (position > 1 && position < mClickedDialogEntryIndex) {
                --mClickedDialogEntryIndex;
            }
        });
        holder.text.setOnClickListener(v -> {
            mClickedDialogEntryIndex = position;
            mCallback.onSelect(position);
        });
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CheckedTextView text;
        private FrameLayout deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            deleteButton = itemView.findViewById(R.id.image_delete_container);
        }
    }
}
