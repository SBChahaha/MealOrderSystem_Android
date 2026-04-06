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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dealorder.R;
import com.example.dealorder.adapter.OrderAdapter;
import com.example.dealorder.db.DBHelper;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {
    private RecyclerView rvOrder;
    private TextView tvEmptyOrder;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        initView(view);
        initDB();
        setupRecyclerView();
        return view;
    }

    private void initView(View view) {
        rvOrder = view.findViewById(R.id.rv_order);
        tvEmptyOrder = view.findViewById(R.id.tv_empty_order);
    }

    private void initDB() {
        dbHelper = new DBHelper(getContext());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(getContext(), orderList);
        rvOrder.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrder.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        orderList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor orderCursor = db.rawQuery("SELECT * FROM orders ORDER BY id DESC", null);

        while (orderCursor.moveToNext()) {
            Order order = new Order();
            order.setId(orderCursor.getInt(orderCursor.getColumnIndexOrThrow("id")));
            order.setOrderTime(orderCursor.getString(orderCursor.getColumnIndexOrThrow("order_time")));
            order.setTotalPrice(orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("total_price")));
            order.setDiscount(orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("discount")));
            order.setFinalPrice(orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("final_price")));
            order.setRemark(orderCursor.getString(orderCursor.getColumnIndexOrThrow("remark")));

            List<Dish> dishList = new ArrayList<>();
            Cursor itemCursor = db.rawQuery("SELECT * FROM order_item WHERE order_id = ?",
                    new String[]{String.valueOf(order.getId())});
            while (itemCursor.moveToNext()) {
                Dish dish = new Dish();
                dish.setId(itemCursor.getInt(itemCursor.getColumnIndexOrThrow("dish_id")));
                dish.setName(itemCursor.getString(itemCursor.getColumnIndexOrThrow("dish_name")));
                dish.setQuantity(itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity")));
                dish.setPrice(itemCursor.getDouble(itemCursor.getColumnIndexOrThrow("price")));
                dish.setTaste(itemCursor.getString(itemCursor.getColumnIndexOrThrow("taste")));
                dishList.add(dish);
            }
            itemCursor.close();
            order.setDishList(dishList);
            orderList.add(order);
        }
        orderCursor.close();
        db.close();

        if (orderList.isEmpty()) {
            tvEmptyOrder.setVisibility(View.VISIBLE);
            rvOrder.setVisibility(View.GONE);
        } else {
            tvEmptyOrder.setVisibility(View.GONE);
            rvOrder.setVisibility(View.VISIBLE);
            orderAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}