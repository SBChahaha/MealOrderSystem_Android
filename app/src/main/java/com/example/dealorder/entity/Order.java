package com.example.dealorder.entity;

import java.util.List;

public class Order {
    private int id;
    private String orderTime;
    private double totalPrice;
    private double discount;
    private double finalPrice;
    private String remark;
    private List<Dish> dishList;

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Dish> getDishList() { return dishList; }
    public void setDishList(List<Dish> dishList) { this.dishList = dishList; }
}