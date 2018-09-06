package com.apps.adrcotfas.goodtime.Statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllEntriesBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesFragment extends Fragment {
    private StatisticsAllEntriesBinding binding;

    private RecyclerView recyclerView;
    private AllEntriesAdapter adapter;
    private List<Session> items;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.statistics_all_entries, container, false);
        View view = binding.getRoot();

        recyclerView = binding.mainRecylcerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        items = new ArrayList<>();

        // TODO: add constructor and add real sessions
        Session s1 = new Session();
        s1.endTime = System.currentTimeMillis();
        s1.totalTime = 125;

        for (int i = 0; i < 15; ++i) {
            items.add(s1);
        }

        adapter = new AllEntriesAdapter(items);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        return view;
    }
}