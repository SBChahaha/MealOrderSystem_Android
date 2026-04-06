package com.example.dealorder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dealorder.R;
import com.example.dealorder.entity.Dish;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ordering.db";
    // 【修改】升级为版本2，自动触发重建，防止修改字段类型后闪退
    private static final int DB_VERSION = 2;

    private static final String CREATE_USER = "CREATE TABLE user_profile (" +
            "id INTEGER PRIMARY KEY, " +
            "points REAL DEFAULT 0)";

    // 【修改】image_url 类型改为 TEXT
    private static final String CREATE_DISH = "CREATE TABLE dish (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "price REAL NOT NULL," +
            "category TEXT NOT NULL," +
            "image_url TEXT," +
            "description TEXT," +
            "is_featured INTEGER DEFAULT 0," +
            "is_set_meal INTEGER DEFAULT 0)";

    private static final String CREATE_ORDERS = "CREATE TABLE orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_time TEXT NOT NULL," +
            "total_price REAL NOT NULL," +
            "discount REAL DEFAULT 0," +
            "final_price REAL NOT NULL," +
            "remark TEXT)";

    private static final String CREATE_ORDER_ITEM = "CREATE TABLE order_item (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER NOT NULL," +
            "dish_id INTEGER NOT NULL," +
            "dish_name TEXT NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "price REAL NOT NULL," +
            "taste TEXT)";

    private static final String CREATE_COUPON = "CREATE TABLE coupon (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "type INTEGER NOT NULL," +
            "condition REAL," +
            "value REAL NOT NULL," +
            "is_used INTEGER DEFAULT 0)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DISH);
        db.execSQL(CREATE_ORDERS);
        db.execSQL(CREATE_ORDER_ITEM);
        db.execSQL(CREATE_COUPON);
        db.execSQL(CREATE_USER);
        db.execSQL("INSERT INTO user_profile (id, points) VALUES (1, 0)");
        initTestData(db);
    }

    public double getPoints() {
        double points = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT points FROM user_profile WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            points = cursor.getDouble(0);
        }
        cursor.close();
        return points;
    }

    public void addPoints(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user_profile SET points = points + ? WHERE id = 1", new Object[]{amount});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS dish");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_item");
        db.execSQL("DROP TABLE IF EXISTS coupon");
        db.execSQL("DROP TABLE IF EXISTS user_profile");
        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS dish");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_item");
        db.execSQL("DROP TABLE IF EXISTS coupon");
        db.execSQL("DROP TABLE IF EXISTS user_profile");
        onCreate(db);
        db.close();
    }

    private void initTestData(SQLiteDatabase db) {
        // 【修改】将 int 强转为 String，兼容旧图片
        insertDish(db, "宫保鸡丁", 28, "家常菜", String.valueOf(R.drawable.dish_gbjd), "经典川菜，鸡肉鲜嫩，花生酥脆", 1, 0);
        insertDish(db, "麻婆豆腐", 22, "川菜", String.valueOf(R.drawable.dish_mpdf), "麻辣鲜香，豆腐嫩滑", 1, 0);
        insertDish(db, "水煮鱼", 58, "川菜", String.valueOf(R.drawable.dish_szy), "鱼片滑嫩，麻辣过瘾", 1, 0);
        insertDish(db, "白切鸡", 38, "粤菜", String.valueOf(R.drawable.dish_bzj), "皮爽肉滑，原汁原味", 0, 0);
        insertDish(db, "清蒸鲈鱼", 68, "粤菜", String.valueOf(R.drawable.dish_qzly), "鱼肉鲜嫩，清淡鲜美", 0, 0);
        insertDish(db, "西红柿炒蛋", 18, "家常菜", String.valueOf(R.drawable.dish_xhscd), "国民家常菜，酸甜可口", 0, 0);
        insertDish(db, "酸辣土豆丝", 16, "家常菜", String.valueOf(R.drawable.dish_tds), "酸辣爽口，下饭神器", 0, 0);
        insertDish(db, "可乐", 6, "饮品", String.valueOf(R.drawable.dish_kl), "冰镇可乐", 0, 0);
        insertDish(db, "米饭", 3, "主食", String.valueOf(R.drawable.dish_rice), "香喷喷的白米饭", 0, 0);
        insertDish(db, "双人套餐A", 88, "套餐", String.valueOf(android.R.drawable.ic_menu_gallery), "宫保鸡丁+麻婆豆腐+米饭2份+可乐2份", 0, 1);
        insertDish(db, "三人套餐B", 128, "套餐", String.valueOf(android.R.drawable.ic_menu_gallery), "水煮鱼+白切鸡+米饭3份+可乐3份", 0, 1);

        insertCoupon(db, "满50减10", 0, 50, 10, 0);
        insertCoupon(db, "满100减25", 0, 100, 25, 0);
        insertCoupon(db, "8折优惠券", 1, 0, 0.8, 0);
    }

    private void insertDish(SQLiteDatabase db, String name, double price, String category,
                            String imageUrl, String description, int isFeatured, int isSetMeal) {
        String sql = "INSERT INTO dish(name, price, category, image_url, description, is_featured, is_set_meal) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        db.execSQL(sql, new Object[]{name, price, category, imageUrl, description, isFeatured, isSetMeal});
    }

    private void insertCoupon(SQLiteDatabase db, String name, int type, double condition, double value, int isUsed) {
        String sql = "INSERT INTO coupon(name, type, condition, value, is_used) VALUES(?, ?, ?, ?, ?)";
        db.execSQL(sql, new Object[]{name, type, condition, value, isUsed});
    }

    public long addDish(Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", dish.getName());
        values.put("price", dish.getPrice());
        values.put("category", dish.getCategory());
        // 【修改】存入String
        values.put("image_url", dish.getImageUrl() != null ? dish.getImageUrl() : "");
        values.put("description", dish.getDescription());
        values.put("is_featured", dish.getIsFeatured());
        values.put("is_set_meal", dish.getIsSetMeal());
        long id = db.insert("dish", null, values);
        db.close();
        return id;
    }

    public int updateDish(Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", dish.getName());
        values.put("price", dish.getPrice());
        values.put("category", dish.getCategory());
        // 【修改】存入String
        values.put("image_url", dish.getImageUrl() != null ? dish.getImageUrl() : "");
        values.put("description", dish.getDescription());
        values.put("is_featured", dish.getIsFeatured());
        values.put("is_set_meal", dish.getIsSetMeal());
        int rows = db.update("dish", values, "id = ?", new String[]{String.valueOf(dish.getId())});
        db.close();
        return rows;
    }

    public void deleteDish(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("dish", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 动态获取目前所有的菜品分类
    public java.util.List<String> getDishCategories() {
        java.util.List<String> categories = new java.util.ArrayList<>();
        categories.add("全部"); // 永远把“全部”放在第一位

        SQLiteDatabase db = this.getReadableDatabase();
        // 使用 DISTINCT 关键字查询去重后的分类名，排除套餐和空值
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM dish WHERE is_set_meal = 0 AND category != '' AND category != '套餐'", null);
        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            categories.add(category);
        }
        cursor.close();
        return categories;
    }
}