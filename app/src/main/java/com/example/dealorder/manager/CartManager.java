package com.example.dealorder.manager;

import com.example.dealorder.entity.Dish;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Dish> cartList = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            synchronized (CartManager.class) {
                if (instance == null) {
                    instance = new CartManager();
                }
            }
        }
        return instance;
    }

    public void addDish(Dish dish) {
        for (Dish d : cartList) {
            if (d.getId() == dish.getId() && d.getTaste().equals(dish.getTaste())) {
                d.setQuantity(d.getQuantity() + dish.getQuantity());
                return;
            }
        }
        cartList.add(dish);
    }

    public void removeDish(int position) {
        cartList.remove(position);
    }

    public void updateQuantity(int position, int quantity) {
        if (quantity <= 0) {
            cartList.remove(position);
        } else {
            cartList.get(position).setQuantity(quantity);
        }
    }

    public void clearCart() {
        cartList.clear();
    }

    public List<Dish> getCartList() {
        return cartList;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Dish dish : cartList) {
            total += dish.getPrice() * dish.getQuantity();
        }
        return total;
    }

    public int getTotalCount() {
        int count = 0;
        for (Dish dish : cartList) {
            count += dish.getQuantity();
        }
        return count;
    }
}