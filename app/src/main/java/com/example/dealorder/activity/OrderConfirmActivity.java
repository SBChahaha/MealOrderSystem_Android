package com.example.dealorder.activity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dealorder.R;
import com.example.dealorder.adapter.OrderItemAdapter;
import com.example.dealorder.db.DBHelper;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.manager.CartManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderConfirmActivity extends AppCompatActivity {
    private RecyclerView rvOrderItems;
    private TextView tvTotalPrice, tvDiscount, tvFinalPrice;
    private EditText etRemark;
    private Button btnSubmitOrder;

    private double totalPrice, discount, finalPrice;
    private int couponId = -1;
    private List<Dish> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        setTitle("确认订单");

        initView();
        getIntentData();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initView() {
        rvOrderItems = findViewById(R.id.rv_order_items);
        tvTotalPrice = findViewById(R.id.tv_confirm_total);
        tvDiscount = findViewById(R.id.tv_confirm_discount);
        tvFinalPrice = findViewById(R.id.tv_confirm_final);
        etRemark = findViewById(R.id.et_remark);
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
    }

    private void getIntentData() {
        totalPrice = getIntent().getDoubleExtra("totalPrice", 0);
        discount = getIntent().getDoubleExtra("discount", 0);
        finalPrice = getIntent().getDoubleExtra("finalPrice", 0);
        couponId = getIntent().getIntExtra("couponId", -1);
        cartList = CartManager.getInstance().getCartList();

        tvTotalPrice.setText(String.format("¥%.2f", totalPrice));
        tvDiscount.setText(String.format("-¥%.2f", discount));
        tvFinalPrice.setText(String.format("¥%.2f", finalPrice));
    }

    private void setupRecyclerView() {
        OrderItemAdapter adapter = new OrderItemAdapter(this, cartList);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSubmitOrder.setOnClickListener(v -> submitOrder());
    }

    private void submitOrder() {
        String remark = etRemark.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
        String orderTime = sdf.format(new Date());

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues orderValues = new ContentValues();
            orderValues.put("order_time", orderTime);
            orderValues.put("total_price", totalPrice);
            orderValues.put("discount", discount);
            orderValues.put("final_price", finalPrice);
            orderValues.put("remark", remark);
            long orderId = db.insert("orders", null, orderValues);

            for (Dish dish : cartList) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("order_id", orderId);
                itemValues.put("dish_id", dish.getId());
                itemValues.put("dish_name", dish.getName());
                itemValues.put("quantity", dish.getQuantity());
                itemValues.put("price", dish.getPrice());
                itemValues.put("taste", dish.getTaste());
                db.insert("order_item", null, itemValues);
            }

            if (couponId != -1) {
                ContentValues couponValues = new ContentValues();
                couponValues.put("is_used", 1);
                db.update("coupon", couponValues, "id = ?", new String[]{String.valueOf(couponId)});
            }

            dbHelper.addPoints(totalPrice);
            db.setTransactionSuccessful();
            Toast.makeText(this, "下单成功！获得积分：" + (int)totalPrice, Toast.LENGTH_SHORT).show();
            CartManager.getInstance().clearCart();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "下单失败，请重试", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}