package com.example.dealorder.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.dealorder.R;
import com.example.dealorder.fragment.CartFragment;
import com.example.dealorder.fragment.HomeFragment;
import com.example.dealorder.fragment.OrderFragment;
import com.example.dealorder.fragment.MineFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFragment();
        setupViewPager();
        setupBottomNav();
    }

    private void initView() {
        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_nav);
        viewPager.setUserInputEnabled(false);
    }

    private void initFragment() {
        fragmentList.add(new HomeFragment());
        fragmentList.add(new CartFragment());
        fragmentList.add(new OrderFragment());
        fragmentList.add(new MineFragment());
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return fragmentList.size();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.nav_order) {
                    viewPager.setCurrentItem(2);
                    return true;
                } else if (itemId == R.id.nav_mine) { // 【新增】点击“我的”切换到第4个页面
                    viewPager.setCurrentItem(3);
                    return true;
                }
                return false;
            }
        });
    }

    public void switchToOrderTab() {
        viewPager.setCurrentItem(2); // 2 代表订单 Fragment 的索引
        bottomNav.setSelectedItemId(R.id.nav_order); // 同步更新底部导航的选中状态
    }
}