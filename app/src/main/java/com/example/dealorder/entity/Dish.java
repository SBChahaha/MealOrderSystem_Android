package com.example.dealorder.entity;

public class Dish {
    private int id;
    private String name;
    private double price;
    private String category;
    private String imageUrl; // 【修改】将 int 改为 String
    private String description;
    private int isFeatured;
    private int isSetMeal;
    private int quantity = 1;
    private String taste = "原味";

    public Dish() {}

    public Dish(int id, String name, double price, String category, String imageUrl,
                String description, int isFeatured, int isSetMeal) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.description = description;
        this.isFeatured = isFeatured;
        this.isSetMeal = isSetMeal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getIsFeatured() { return isFeatured; }
    public void setIsFeatured(int isFeatured) { this.isFeatured = isFeatured; }
    public int getIsSetMeal() { return isSetMeal; }
    public void setIsSetMeal(int isSetMeal) { this.isSetMeal = isSetMeal; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTaste() { return taste; }
    public void setTaste(String taste) { this.taste = taste; }
}