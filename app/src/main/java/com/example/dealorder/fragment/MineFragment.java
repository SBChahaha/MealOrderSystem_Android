package com.example.dealorder.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dealorder.R;
import com.example.dealorder.activity.DishEditActivity;
import com.example.dealorder.activity.MainActivity;
import com.example.dealorder.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class MineFragment extends Fragment {
    private TextView tvMineCoupon;
    private TextView tvMinePoints;
    private LinearLayout layoutCoupon;
    private TextView tvMineOrders, tvMineAddDish, tvMineAddress, tvMineService;

    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        initView(view);
        initDB();
        setupClickListeners();
        return view;
    }

    private void initView(View view) {
        tvMineCoupon = view.findViewById(R.id.tv_mine_coupon);
        tvMinePoints = view.findViewById(R.id.tv_mine_points);
        layoutCoupon = view.findViewById(R.id.layout_coupon);

        tvMineOrders = view.findViewById(R.id.tv_mine_orders);
        tvMineAddDish = view.findViewById(R.id.tv_mine_add_dish);
        tvMineAddress = view.findViewById(R.id.tv_mine_address);
        tvMineService = view.findViewById(R.id.tv_mine_service);
    }

    private void initDB() {
        dbHelper = new DBHelper(getContext());
    }

    private void setupClickListeners() {
        // 点击优惠券区域，弹出具体的可用优惠券列表
        layoutCoupon.setOnClickListener(v -> showCouponDetailsDialog());

        // 点击“我的订单”，联动切换到MainActivity的订单Tab
        tvMineOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToOrderTab();
            }
        });

        // 商家功能：添加新菜品
        tvMineAddDish.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), DishEditActivity.class));
        });

        // 占位功能扩展
        tvMineAddress.setOnClickListener(v -> Toast.makeText(getContext(), "功能开发中：收货地址管理", Toast.LENGTH_SHORT).show());
        tvMineService.setOnClickListener(v -> Toast.makeText(getContext(), "功能开发中：联系客服", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次切回到这个页面，都重新读取一遍数据库，保证数据最新
        loadCouponCount();
        displayPoints();
    }

    // 查询未使用的优惠券数量
    private void loadCouponCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM coupon WHERE is_used = 0", null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            tvMineCoupon.setText(String.valueOf(count));
        }
        cursor.close();
        db.close();
    }

    // 显示当前积分
    private void displayPoints() {
        double points = dbHelper.getPoints();
        tvMinePoints.setText(String.valueOf((int) points));
    }

    // 弹出对话框显示剩余优惠券具体是什么
    private void showCouponDetailsDialog() {
        List<String> couponDetails = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 查询所有未使用的优惠券明细
        Cursor cursor = db.rawQuery("SELECT name, condition, value, type FROM coupon WHERE is_used = 0", null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            double cond = cursor.getDouble(1);
            double val = cursor.getDouble(2);
            int type = cursor.getInt(3);

            // 组装优惠券显示文字
            String desc = type == 0 ?
                    String.format("%s (满%.0f减%.0f)", name, cond, val) :
                    String.format("%s (%.1f折)", name, val * 10);
            couponDetails.add(desc);
        }
        cursor.close();
        db.close();

        // 判空处理
        if (couponDetails.isEmpty()) {
            Toast.makeText(getContext(), "暂时没有可用的优惠券哦", Toast.LENGTH_SHORT).show();
            return;
        }

        // 弹出系统自带的列表对话框
        new AlertDialog.Builder(getContext())
                .setTitle("我的可用优惠券")
                .setItems(couponDetails.toArray(new String[0]), null)
                .setPositiveButton("知道了", null)
                .show();
    }
}