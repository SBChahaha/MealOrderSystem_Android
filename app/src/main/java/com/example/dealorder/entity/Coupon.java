package com.example.dealorder.entity;

public class Coupon {
    private int id;
    private String name;
    private int type;
    private double condition;
    private double value;
    private int isUsed;

    public Coupon() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public double getCondition() { return condition; }
    public void setCondition(double condition) { this.condition = condition; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public int getIsUsed() { return isUsed; }
    public void setIsUsed(int isUsed) { this.isUsed = isUsed; }
}