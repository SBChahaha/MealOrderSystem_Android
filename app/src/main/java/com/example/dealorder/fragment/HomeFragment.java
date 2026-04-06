package com.example.dealorder.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;
import com.example.dealorder.R;
import com.example.dealorder.adapter.DishAdapter;
import com.example.dealorder.adapter.FeaturedAdapter;
import com.example.dealorder.db.DBHelper;
import com.example.dealorder.entity.Dish;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private Banner banner;
    private TabLayout tabLayout;
    private RecyclerView rvDish;
    private TextView tvSetMealTitle;
    private RecyclerView rvSetMeal;
    private android.widget.Button btnResetDb;

    private DBHelper dbHelper;
    private List<Dish> featuredList = new ArrayList<>();
    private List<Dish> dishList = new ArrayList<>();
    private List<Dish> setMealList = new ArrayList<>();
    private DishAdapter dishAdapter;
    private DishAdapter setMealAdapter;

    // 【删除】原来这里有一个写死的 categories 数组，现在删掉了，改成去数据库动态拿

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
        initDB();
        setupRecyclerView();
        loadData();
        return view;
    }

    private void initView(View view) {
        banner = view.findViewById(R.id.banner);
        tabLayout = view.findViewById(R.id.tab_layout);
        rvDish = view.findViewById(R.id.rv_dish);
        tvSetMealTitle = view.findViewById(R.id.tv_set_meal_title);
        rvSetMeal = view.findViewById(R.id.rv_set_meal);

        btnResetDb = view.findViewById(R.id.btn_reset_db);
        btnResetDb.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("警告")
                    .setMessage("确定要重置系统吗？所有订单和购物车数据将被清空，菜品恢复为初始状态。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        if (dbHelper != null) {
                            dbHelper.resetDatabase();
                        }
                        com.example.dealorder.manager.CartManager.getInstance().clearCart();
                        loadData();
                        android.widget.Toast.makeText(getContext(), "系统已重置！", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void initDB() {
        dbHelper = new DBHelper(getContext());
    }

    private void loadData() {
        // 1. 记录刷新前选中的分类（防止刷新后强制跳回"全部"）
        String currentCategory = "全部";
        if (tabLayout != null && tabLayout.getSelectedTabPosition() >= 0 && tabLayout.getTabCount() > 0) {
            currentCategory = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
        }

        // 2. 动态生成 TabLayout 标签
        setupDynamicTabs(currentCategory);

        // 3. 加载其余数据
        loadFeaturedDishes();
        loadSetMeals();
        setupBanner();
    }

    // 【新增】核心逻辑：动态生成分类 Tab
    private void setupDynamicTabs(String currentCategory) {
        // 从数据库实时提取所有分类
        List<String> dynamicCategories = dbHelper.getDishCategories();

        // 移除旧的监听器和所有标签，准备重新生成
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.removeAllTabs();

        int selectedIndex = 0;
        for (int i = 0; i < dynamicCategories.size(); i++) {
            String cat = dynamicCategories.get(i);
            tabLayout.addTab(tabLayout.newTab().setText(cat));
            // 找到之前用户停留的那个分类对应的位置
            if (cat.equals(currentCategory)) {
                selectedIndex = i;
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadDishesByCategory(tab.getText().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 选中计算好的分类。如果该分类已经被删了，会自动跳回 0 (即"全部")
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(selectedIndex).select();
            // 主动触发一次列表加载，保证数据对应
            loadDishesByCategory(dynamicCategories.get(selectedIndex));
        }
    }

    private void loadFeaturedDishes() {
        featuredList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dish WHERE is_featured = 1", null);
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            dish.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            dish.setIsFeatured(cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")));
            dish.setIsSetMeal(cursor.getInt(cursor.getColumnIndexOrThrow("is_set_meal")));
            featuredList.add(dish);
        }
        cursor.close();
        db.close();
    }

    private void loadDishesByCategory(String category) {
        dishList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = category.equals("全部") ?
                "SELECT * FROM dish WHERE is_set_meal = 0" :
                "SELECT * FROM dish WHERE category = ? AND is_set_meal = 0";
        Cursor cursor = db.rawQuery(sql, category.equals("全部") ? null : new String[]{category});
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            dish.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            dish.setIsFeatured(cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")));
            dish.setIsSetMeal(cursor.getInt(cursor.getColumnIndexOrThrow("is_set_meal")));
            dishList.add(dish);
        }
        cursor.close();
        db.close();
        if (dishAdapter != null) dishAdapter.notifyDataSetChanged();
    }

    private void loadSetMeals() {
        setMealList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dish WHERE is_set_meal = 1", null);
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            dish.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            dish.setIsFeatured(cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")));
            dish.setIsSetMeal(cursor.getInt(cursor.getColumnIndexOrThrow("is_set_meal")));
            setMealList.add(dish);
        }
        cursor.close();
        db.close();
        if (setMealAdapter != null) setMealAdapter.notifyDataSetChanged();
    }

    private void setupBanner() {
        if (featuredList.isEmpty()) {
            banner.setVisibility(View.GONE);
            return;
        }
        banner.setVisibility(View.VISIBLE);
        banner.setAdapter(new FeaturedAdapter(featuredList, getContext()))
                .setIndicator(new CircleIndicator(getContext()))
                .setLoopTime(3000)
                .start();
    }

    private void setupRecyclerView() {
        dishAdapter = new DishAdapter(getContext(), dishList);
        rvDish.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvDish.setAdapter(dishAdapter);

        // 套餐：使用 R.layout.item_set_meal 让海报图更大
        setMealAdapter = new DishAdapter(getContext(), setMealList, R.layout.item_set_meal);
        rvSetMeal.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSetMeal.setAdapter(setMealAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 只要返回首页，就去刷新一次所有数据和动态分类标签
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}