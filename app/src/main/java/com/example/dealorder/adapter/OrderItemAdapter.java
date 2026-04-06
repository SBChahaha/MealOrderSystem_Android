package com.example.dealorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dealorder.R;
import com.example.dealorder.entity.Dish;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private Context context;
    private List<Dish> dishList;

    public OrderItemAdapter(Context context, List<Dish> dishList) {
        this.context = context;
        this.dishList = dishList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Dish dish = dishList.get(position);
        holder.tvName.setText(dish.getName());
        holder.tvTaste.setText("口味：" + dish.getTaste());
        holder.tvQuantity.setText("x" + dish.getQuantity());
        holder.tvSubtotal.setText(String.format("¥%.2f", dish.getPrice() * dish.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTaste, tvQuantity, tvSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_order_item_name);
            tvTaste = itemView.findViewById(R.id.tv_order_item_taste);
            tvQuantity = itemView.findViewById(R.id.tv_order_item_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_order_item_subtotal);
        }
    }
}