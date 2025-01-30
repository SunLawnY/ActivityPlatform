package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.eventapp.adapter.EventAdapter;
import com.example.eventapp.database.DatabaseHelper;
import com.example.eventapp.fragment.ProfileFragment;
import com.example.eventapp.model.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.RadioGroup;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private DatabaseHelper dbHelper;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar loadingView;
    private TextView emptyView;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences prefs;
    private TextInputEditText searchInput;
    private ChipGroup timeFilterChipGroup;
    private String currentKeyword = "";
    private String currentTimeFilter = "";
    private String currentStatusFilter = "";
    private View fragmentContainer;
    private View searchLayout;
    private View filterButton;
    private View chipGroupContainer;

    private final ActivityResultLauncher<Intent> createEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadEvents();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();
        // 设置工具栏
        setupToolbar();
        // 设置底部导航
        setupBottomNavigation();
        // 设置RecyclerView
        setupRecyclerView();
        // 设置下拉刷新
        setupSwipeRefresh();
        // 设置搜索和筛选
        setupSearchAndFilter();
        
        // 加载活动数据
        loadEvents();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        loadingView = findViewById(R.id.loadingView);
        emptyView = findViewById(R.id.emptyView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        searchInput = findViewById(R.id.searchInput);
        timeFilterChipGroup = findViewById(R.id.timeFilterChipGroup);
        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        searchLayout = findViewById(R.id.searchLayout);
        filterButton = findViewById(R.id.filterButton);
        chipGroupContainer = findViewById(R.id.chipGroupContainer);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event List");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_events) {
                // 显示活动列表
                swipeRefresh.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.VISIBLE);
                filterButton.setVisibility(View.VISIBLE);
                chipGroupContainer.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
                
                // 移除当前的Fragment（如果有）
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .remove(currentFragment)
                        .commit();
                }
                loadEvents();
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                // 显示我的活动
                swipeRefresh.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.VISIBLE);
                filterButton.setVisibility(View.VISIBLE);
                chipGroupContainer.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
                
                // 移除当前的Fragment（如果有）
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .remove(currentFragment)
                        .commit();
                }
                loadMyEvents();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // 显示个人资料
                swipeRefresh.setVisibility(View.GONE);
                searchLayout.setVisibility(View.GONE);
                filterButton.setVisibility(View.GONE);
                chipGroupContainer.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.VISIBLE);
                
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProfileFragment())
                    .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem createItem = menu.findItem(R.id.action_create);
        createItem.setVisible(prefs.getBoolean("isStaff", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            Intent intent = new Intent(this, CreateEventActivity.class);
            createEventLauncher.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(this, new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadEvents);
    }

    private void setupSearchAndFilter() {
        // 设置搜索功能
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKeyword = searchInput.getText().toString().trim();
                loadEvents();
                return true;
            }
            return false;
        });

        // 设置时间筛选
        timeFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentTimeFilter = "";
            } else if (checkedId == R.id.chipToday) {
                currentTimeFilter = "today";
            } else if (checkedId == R.id.chipWeek) {
                currentTimeFilter = "week";
            } else if (checkedId == R.id.chipMonth) {
                currentTimeFilter = "month";
            }
            loadEvents();
        });

        // 设置筛选按钮
        findViewById(R.id.filterButton).setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_filter, null);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        RadioGroup statusGroup = dialogView.findViewById(R.id.statusGroup);
        MaterialButton resetButton = dialogView.findViewById(R.id.resetButton);
        MaterialButton applyButton = dialogView.findViewById(R.id.applyButton);

        // 设置当前选中状态
        switch (currentStatusFilter) {
            case "not_started":
                statusGroup.check(R.id.statusNotStarted);
                break;
            case "ongoing":
                statusGroup.check(R.id.statusOngoing);
                break;
            case "ended":
                statusGroup.check(R.id.statusEnded);
                break;
            case "full":
                statusGroup.check(R.id.statusFull);
                break;
            default:
                statusGroup.check(R.id.statusAll);
                break;
        }

        // 重置按钮
        resetButton.setOnClickListener(v -> {
            statusGroup.check(R.id.statusAll);
        });

        // 应用按钮
        applyButton.setOnClickListener(v -> {
            int checkedId = statusGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.statusAll) {
                currentStatusFilter = "";
            } else if (checkedId == R.id.statusNotStarted) {
                currentStatusFilter = "not_started";
            } else if (checkedId == R.id.statusOngoing) {
                currentStatusFilter = "ongoing";
            } else if (checkedId == R.id.statusEnded) {
                currentStatusFilter = "ended";
            } else if (checkedId == R.id.statusFull) {
                currentStatusFilter = "full";
            }
            loadEvents();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadEvents() {
        // 显示加载中
        loadingView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // 在后台线程加载数据
        new Thread(() -> {
            List<Event> events = dbHelper.searchEvents(currentKeyword, currentTimeFilter, currentStatusFilter);
            // 在主线程更新UI
            runOnUiThread(() -> {
                adapter.updateEvents(events);
                loadingView.setVisibility(View.GONE);
                if (events.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                swipeRefresh.setRefreshing(false);
            });
        }).start();
    }

    private void loadMyEvents() {
        // 显示加载中
        loadingView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // 在后台线程加载数据
        new Thread(() -> {
            long userId = prefs.getLong("userId", -1);
            List<Event> events = dbHelper.getUserRegisteredEvents(userId);
            // 在主线程更新UI
            runOnUiThread(() -> {
                adapter.updateEvents(events);
                loadingView.setVisibility(View.GONE);
                if (events.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                swipeRefresh.setRefreshing(false);
            });
        }).start();
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(); // 每次返回主界面时刷新活动列表
    }
} 