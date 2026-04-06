package com.example.dealorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dealorder.R;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.entity.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("订单号：" + order.getId());
        holder.tvOrderTime.setText(order.getOrderTime());
        holder.tvFinalPrice.setText(String.format("¥%.2f", order.getFinalPrice()));

        int totalCount = 0;
        for (Dish dish : order.getDishList()) {
            totalCount += dish.getQuantity();
        }
        holder.tvTotalCount.setText(String.valueOf(totalCount));

        OrderItemAdapter itemAdapter = new OrderItemAdapter(context, order.getDishList());
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvOrderItems.setAdapter(itemAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderTime, tvTotalCount, tvFinalPrice;
        RecyclerView rvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvTotalCount = itemView.findViewById(R.id.tv_order_total_count);
            tvFinalPrice = itemView.findViewById(R.id.tv_order_final_price);
            rvOrderItems = itemView.findViewById(R.id.rv_order_items);
        }
    }
}