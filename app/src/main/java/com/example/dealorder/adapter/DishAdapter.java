package com.example.dealorder.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dealorder.R;
import com.example.dealorder.activity.DishEditActivity;
import com.example.dealorder.entity.Dish;
import com.example.dealorder.manager.CartManager;

import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {
    private Context context;
    private List<Dish> dishList;
    private int layoutResId; // 【新增】用于保存布局文件的 ID

    // 【修改】原有的默认构造函数，默认使用普通菜品布局，保证之前的代码不报错
    public DishAdapter(Context context, List<Dish> dishList) {
        this.context = context;
        this.dishList = dishList;
        this.layoutResId = R.layout.item_dish;
    }

    // 【新增】带布局参数的构造函数，专门留给套餐使用
    public DishAdapter(Context context, List<Dish> dishList, int layoutResId) {
        this.context = context;
        this.dishList = dishList;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 【修改】这里不要写死 R.layout.item_dish，而是使用变量 layoutResId
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishList.get(position);
        holder.tvName.setText(dish.getName());
        holder.tvPrice.setText(String.format("¥%.2f", dish.getPrice()));
        holder.tvDesc.setText(dish.getDescription());

        // 【修改】兼容新老图片加载
        String imgUrl = dish.getImageUrl();
        if (imgUrl != null && imgUrl.matches("\\d+")) {
            Glide.with(context).load(Integer.parseInt(imgUrl)).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        } else if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context).load(imgUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        } else {
            Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(holder.ivDish);
        }

        holder.itemView.setOnClickListener(v -> showDishSelectDialog(dish));

        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(context, DishEditActivity.class);
            intent.putExtra("dish_id", dish.getId());
            intent.putExtra("dish_name", dish.getName());
            intent.putExtra("dish_price", dish.getPrice());
            intent.putExtra("dish_category", dish.getCategory());
            intent.putExtra("dish_desc", dish.getDescription());
            intent.putExtra("dish_featured", dish.getIsFeatured());
            intent.putExtra("dish_set_meal", dish.getIsSetMeal());
            intent.putExtra("dish_image_url", dish.getImageUrl()); // 【新增】传递图片地址
            context.startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDish;
        TextView tvName, tvPrice, tvDesc;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDish = itemView.findViewById(R.id.iv_dish);
            tvName = itemView.findViewById(R.id.tv_dish_name);
            tvPrice = itemView.findViewById(R.id.tv_dish_price);
            tvDesc = itemView.findViewById(R.id.tv_dish_desc);
        }
    }

    private void showDishSelectDialog(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_dish_select, null);
        builder.setView(dialogView);

        ImageView ivDialogDish = dialogView.findViewById(R.id.iv_dialog_dish);
        TextView tvDialogName = dialogView.findViewById(R.id.tv_dialog_name);
        TextView tvDialogPrice = dialogView.findViewById(R.id.tv_dialog_price);
        TextView tvQuantity = dialogView.findViewById(R.id.tv_quantity);
        Button btnMinus = dialogView.findViewById(R.id.btn_minus);
        Button btnPlus = dialogView.findViewById(R.id.btn_plus);
        RadioGroup rgTaste = dialogView.findViewById(R.id.rg_taste);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAddCart = dialogView.findViewById(R.id.btn_add_cart);

        // 【修改】弹窗中的图片加载兼容
        String imgUrl = dish.getImageUrl();
        if (imgUrl != null && imgUrl.matches("\\d+")) {
            Glide.with(context).load(Integer.parseInt(imgUrl)).into(ivDialogDish);
        } else if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context).load(imgUrl).into(ivDialogDish);
        } else {
            Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(ivDialogDish);
        }

        tvDialogName.setText(dish.getName());
        tvDialogPrice.setText(String.format("¥%.2f", dish.getPrice()));
        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity[0] < 10) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAddCart.setOnClickListener(v -> {
            int checkedId = rgTaste.getCheckedRadioButtonId();
            RadioButton rbTaste = dialogView.findViewById(checkedId);
            String taste = rbTaste.getText().toString();

            Dish cartDish = new Dish();
            cartDish.setId(dish.getId());
            cartDish.setName(dish.getName());
            cartDish.setPrice(dish.getPrice());
            cartDish.setImageUrl(dish.getImageUrl());
            cartDish.setQuantity(quantity[0]);
            cartDish.setTaste(taste);

            CartManager.getInstance().addDish(cartDish);
            Toast.makeText(context, "已添加到购物车", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
}