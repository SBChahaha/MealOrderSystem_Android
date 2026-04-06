package com.example.dealorder.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dealorder.R;
import com.example.dealorder.activity.OrderConfirmActivity;
import com.example.dealorder.adapter.CartAdapter;
import com.example.dealorder.db.DBHelper;
import com.example.dealorder.entity.Coupon;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.manager.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView rvCart;
    private TextView tvEmptyCart;
    private TextView tvTotalPrice, tvDiscount, tvFinalPrice;
    private Button btnClearCart, btnSelectCoupon, btnSettle;

    private CartAdapter cartAdapter;
    private Coupon selectedCoupon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        initView(view);
        setupRecyclerView();
        updateCartInfo();
        setupClickListeners();
        return view;
    }

    private void initView(View view) {
        rvCart = view.findViewById(R.id.rv_cart);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        tvDiscount = view.findViewById(R.id.tv_discount);
        tvFinalPrice = view.findViewById(R.id.tv_final_price);
        btnClearCart = view.findViewById(R.id.btn_clear_cart);
        btnSelectCoupon = view.findViewById(R.id.btn_select_coupon);
        btnSettle = view.findViewById(R.id.btn_settle);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(getContext(), CartManager.getInstance().getCartList(), this::updateCartInfo);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);
    }

    public void updateCartInfo() {
        List<Dish> cartList = CartManager.getInstance().getCartList();
        if (cartList.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            btnClearCart.setEnabled(false);
            btnSelectCoupon.setEnabled(false);
            btnSettle.setEnabled(false);
            tvTotalPrice.setText("¥0.00");
            tvDiscount.setText("-¥0.00");
            tvFinalPrice.setText("¥0.00");
            selectedCoupon = null;
            btnSelectCoupon.setText(R.string.select_coupon);
            return;
        }

        tvEmptyCart.setVisibility(View.GONE);
        rvCart.setVisibility(View.VISIBLE);
        btnClearCart.setEnabled(true);
        btnSelectCoupon.setEnabled(true);
        btnSettle.setEnabled(true);

        double totalPrice = CartManager.getInstance().getTotalPrice();
        double discount = calculateDiscount(totalPrice);
        double finalPrice = totalPrice - discount;

        tvTotalPrice.setText(String.format("¥%.2f", totalPrice));
        tvDiscount.setText(String.format("-¥%.2f", discount));
        tvFinalPrice.setText(String.format("¥%.2f", finalPrice));
        cartAdapter.notifyDataSetChanged();
    }

    private double calculateDiscount(double totalPrice) {
        if (selectedCoupon == null) return 0;
        if (selectedCoupon.getType() == 0) {
            if (totalPrice >= selectedCoupon.getCondition()) {
                return selectedCoupon.getValue();
            }
        } else if (selectedCoupon.getType() == 1) {
            return totalPrice * (1 - selectedCoupon.getValue());
        }
        return 0;
    }

    private void setupClickListeners() {
        btnClearCart.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            selectedCoupon = null;
            btnSelectCoupon.setText("选择优惠券");
            updateCartInfo();
            Toast.makeText(getContext(), "购物车已清空", Toast.LENGTH_SHORT).show();
        });

        btnSelectCoupon.setOnClickListener(v -> showCouponSelectDialog());

        btnSettle.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OrderConfirmActivity.class);
            double totalPrice = CartManager.getInstance().getTotalPrice();
            double discount = calculateDiscount(totalPrice);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("discount", discount);
            intent.putExtra("finalPrice", totalPrice - discount);
            if (selectedCoupon != null) {
                intent.putExtra("couponId", selectedCoupon.getId());
            }
            startActivity(intent);
        });
    }

    private void showCouponSelectDialog() {
        List<Coupon> couponList = loadAvailableCoupons();
        if (couponList.isEmpty()) {
            Toast.makeText(getContext(), "暂无可用优惠券", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] couponNames = new String[couponList.size() + 1];
        couponNames[0] = "不使用优惠券";
        for (int i = 0; i < couponList.size(); i++) {
            Coupon coupon = couponList.get(i);
            couponNames[i + 1] = coupon.getType() == 0 ?
                    String.format("%s (满%.0f减%.0f)", coupon.getName(), coupon.getCondition(), coupon.getValue()) :
                    String.format("%s (%.0f折)", coupon.getName(), coupon.getValue() * 10);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("选择优惠券")
                .setItems(couponNames, (dialog, which) -> {
                    if (which == 0) {
                        selectedCoupon = null;
                        btnSelectCoupon.setText("选择优惠券");
                    } else {
                        selectedCoupon = couponList.get(which - 1);
                        btnSelectCoupon.setText(selectedCoupon.getName());
                    }
                    updateCartInfo();
                })
                .show();
    }

    private List<Coupon> loadAvailableCoupons() {
        List<Coupon> couponList = new ArrayList<>();
        SQLiteDatabase db = new DBHelper(getContext()).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM coupon WHERE is_used = 0", null);
        while (cursor.moveToNext()) {
            Coupon coupon = new Coupon();
            coupon.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            coupon.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            coupon.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
            coupon.setCondition(cursor.getDouble(cursor.getColumnIndexOrThrow("condition")));
            coupon.setValue(cursor.getDouble(cursor.getColumnIndexOrThrow("value")));
            coupon.setIsUsed(cursor.getInt(cursor.getColumnIndexOrThrow("is_used")));
            couponList.add(coupon);
        }
        cursor.close();
        db.close();
        return couponList;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartInfo();
    }
}