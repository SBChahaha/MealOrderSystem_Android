package com.example.dealorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dealorder.R;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.manager.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Dish> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<Dish> cartList, OnCartChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Dish dish = cartList.get(position);
        holder.tvName.setText(dish.getName());
        holder.tvTaste.setText("口味：" + dish.getTaste());
        holder.tvPrice.setText(String.format("¥%.2f", dish.getPrice()));
        holder.tvQuantity.setText(String.valueOf(dish.getQuantity()));
        String imgUrl = dish.getImageUrl();
        if (imgUrl != null && imgUrl.matches("\\d+")) {
            Glide.with(context).load(Integer.parseInt(imgUrl)).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        } else if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context).load(imgUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        } else {
            Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        }

        holder.btnMinus.setOnClickListener(v -> {
            int newQuantity = dish.getQuantity() - 1;
            CartManager.getInstance().updateQuantity(position, newQuantity);
            listener.onCartChanged();
        });

        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = dish.getQuantity() + 1;
            if (newQuantity <= 10) {
                dish.setQuantity(newQuantity);
                listener.onCartChanged();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().removeDish(position);
            listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDish;
        TextView tvName, tvTaste, tvPrice, tvQuantity;
        Button btnMinus, btnPlus, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDish = itemView.findViewById(R.id.iv_cart_dish);
            tvName = itemView.findViewById(R.id.tv_cart_name);
            tvTaste = itemView.findViewById(R.id.tv_cart_taste);
            tvPrice = itemView.findViewById(R.id.tv_cart_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            btnDelete = itemView.findViewById(R.id.btn_cart_delete);
        }
    }
}