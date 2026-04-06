package com.example.dealorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dealorder.R;
import com.example.dealorder.entity.Dish;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class FeaturedAdapter extends BannerAdapter<Dish, FeaturedAdapter.FeaturedViewHolder> {
    private Context context;

    public FeaturedAdapter(List<Dish> datas, Context context) {
        super(datas);
        this.context = context;
    }

    @Override
    public FeaturedViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindView(FeaturedViewHolder holder, Dish data, int position, int size) {
        holder.tvName.setText(data.getName());
        holder.tvPrice.setText(String.format("¥%.2f", data.getPrice()));
        String imgUrl = data.getImageUrl();
        if (imgUrl != null && imgUrl.matches("\\d+")) {
            Glide.with(context).load(Integer.parseInt(imgUrl)).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImage);
        } else if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context).load(imgUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImage);
        } else {
            Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(holder.ivImage);
        }
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;

        public FeaturedViewHolder(@NonNull View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_featured);
            tvName = view.findViewById(R.id.tv_featured_name);
            tvPrice = view.findViewById(R.id.tv_featured_price);
        }
    }
}