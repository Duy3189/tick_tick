package hcmute.edu.vn.tick_tick.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Calendar;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class HomeFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter taskAdapter;
    private TaskAdapter completedAdapter;
    private TextView tvGreeting, tvDate, tvTaskCount, tvProgressPercent;
    private LinearLayout emptyStateContainer, completedSection;
    private RecyclerView rvTasks, rvCompleted;
    private CircularProgressIndicator progressTasks;
    private MaterialCardView cardStats, cardPending;
    
    private int totalTasks = 0;
    private int completedTasks = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Initialize views
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvDate = view.findViewById(R.id.tv_date);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        completedSection = view.findViewById(R.id.completed_section);
        rvTasks = view.findViewById(R.id.rv_tasks);
        rvCompleted = view.findViewById(R.id.rv_completed);
        progressTasks = view.findViewById(R.id.progress_tasks);
        cardStats = view.findViewById(R.id.card_stats);
        cardPending = view.findViewById(R.id.card_pending);

        // Set greeting with animation
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText(R.string.greeting_morning);
        else if (hour < 17) tvGreeting.setText(R.string.greeting_afternoon);
        else tvGreeting.setText(R.string.greeting_evening);
        
        // Animate greeting
        tvGreeting.setAlpha(0f);
        tvGreeting.setTranslationY(-20f);
        tvGreeting.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();

        // Set date
        tvDate.setText(DateFormat.format("EEEE, MMMM d", Calendar.getInstance()));

        // Animate pending card
        if (cardPending != null) {
            cardPending.setAlpha(0f);
            cardPending.setTranslationX(-30f);
            cardPending.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }
        
        // Animate stats card
        if (cardStats != null) {
            cardStats.setAlpha(0f);
            cardStats.setTranslationX(30f);
            cardStats.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }

        // Task list
        taskAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);
        rvTasks.setItemAnimator(null); // We handle animations ourselves

        // Completed list
        completedAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        completedAdapter.setCompletedMode(true);
        rvCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCompleted.setAdapter(completedAdapter);

        // Observe today tasks
        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            totalTasks = tasks != null ? tasks.size() : 0;
            
            boolean isEmpty = tasks == null || tasks.isEmpty();
            emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            
            updateStats();
        });

        // Observe completed
        viewModel.getTodayCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            completedAdapter.submitList(tasks);
            completedTasks = tasks != null ? tasks.size() : 0;
            
            boolean show = tasks != null && !tasks.isEmpty();
            completedSection.setVisibility(show ? View.VISIBLE : View.GONE);
            
            updateStats();
        });
        
        // View All click listener
        TextView tvViewAll = view.findViewById(R.id.tv_view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                // Navigate to All Tasks - handled by activity
                if (getActivity() != null) {
                    // You can implement navigation here
                }
            });
        }
    }
    
    private void updateStats() {
        int total = totalTasks + completedTasks;
        tvTaskCount.setText(String.valueOf(totalTasks));
        
        if (total > 0) {
            int percent = (int) ((completedTasks * 100.0f) / total);
            tvProgressPercent.setText(percent + "%");
            
            // Animate progress
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressTasks, "progress", progressTasks.getProgress(), percent);
            progressAnimator.setDuration(500);
            progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            progressAnimator.start();
        } else {
            tvProgressPercent.setText("0%");
            progressTasks.setProgress(0);
        }
    }
}
