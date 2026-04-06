package com.example.dealorder.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dealorder.R;
import com.example.dealorder.db.DBHelper;
import com.example.dealorder.entity.Dish;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DishEditActivity extends AppCompatActivity {

    private EditText etName, etPrice, etCategory, etDesc;
    private CheckBox cbFeatured, cbSetMeal;
    private Button btnSave, btnDelete, btnAiGenerate;
    private ImageView ivPreview;

    private DBHelper dbHelper;
    private boolean isEditMode = false;
    private int dishIdToEdit = -1;

    // 图片存储
    private String selectedImageUri = "";
    private static final int PICK_IMAGE_REQUEST = 100;

    // ==========================================
    // 【修改】智谱 GLM-Image (CogView) API 配置
    // 请在此处填入你在智谱开放平台申请的 API Key
    // ==========================================
    private static final String GLM_API_KEY = "4ade6b1201794feb93e579d1f8bfb281.UP8KPugjAEs5pClC";
    private static final String GLM_API_URL = "https://open.bigmodel.cn/api/paas/v4/images/generations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_edit);

        dbHelper = new DBHelper(this);
        initView();
        setupListeners();
        checkModeAndLoadData();
    }

    private void initView() {
        ivPreview = findViewById(R.id.iv_dish_preview);
        btnAiGenerate = findViewById(R.id.btn_ai_generate);
        etName = findViewById(R.id.et_dish_name);
        etPrice = findViewById(R.id.et_dish_price);
        etCategory = findViewById(R.id.et_dish_category);
        etDesc = findViewById(R.id.et_dish_desc);
        cbFeatured = findViewById(R.id.cb_is_featured);
        cbSetMeal = findViewById(R.id.cb_is_set_meal);
        btnSave = findViewById(R.id.btn_save_dish);
        btnDelete = findViewById(R.id.btn_delete_dish);
    }

    private void checkModeAndLoadData() {
        dishIdToEdit = getIntent().getIntExtra("dish_id", -1);
        if (dishIdToEdit != -1) {
            isEditMode = true;
            setTitle("修改菜品");
            btnDelete.setVisibility(View.VISIBLE);

            etName.setText(getIntent().getStringExtra("dish_name"));
            etPrice.setText(String.valueOf(getIntent().getDoubleExtra("dish_price", 0)));
            etCategory.setText(getIntent().getStringExtra("dish_category"));
            etDesc.setText(getIntent().getStringExtra("dish_desc"));
            cbFeatured.setChecked(getIntent().getIntExtra("dish_featured", 0) == 1);
            cbSetMeal.setChecked(getIntent().getIntExtra("dish_set_meal", 0) == 1);

            selectedImageUri = getIntent().getStringExtra("dish_image_url");
            if (selectedImageUri != null && selectedImageUri.matches("\\d+")) {
                Glide.with(this).load(Integer.parseInt(selectedImageUri)).into(ivPreview);
            } else if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                Glide.with(this).load(selectedImageUri).into(ivPreview);
            }
        } else {
            setTitle("添加新菜品");
        }
    }

    private void setupListeners() {
        // 1. 点击图片：打开本地相册
        ivPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // 2. 点击 AI 按钮：调用智谱 API
        btnAiGenerate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "请先输入菜品名称！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (GLM_API_KEY.contains("你的")) {
                Toast.makeText(this, "请先在代码中填写真实的智谱 API_KEY", Toast.LENGTH_LONG).show();
                return;
            }

            generateImageWithGLM(name, category, desc);
        });

        btnSave.setOnClickListener(v -> saveDish());

        btnDelete.setOnClickListener(v -> {
            dbHelper.deleteDish(dishIdToEdit);
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            finish();
        });

        cbSetMeal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etCategory.setText("套餐");
                etCategory.setEnabled(false);
            } else {
                etCategory.setEnabled(true);
                if (etCategory.getText().toString().equals("套餐")) {
                    etCategory.setText("");
                }
            }
        });
    }

    // 【修改】智谱 GLM API 调用方法
    private void generateImageWithGLM(String name, String category, String desc) {
        Toast.makeText(this, "智谱 AI 正在为您作画，请耐心等待...", Toast.LENGTH_LONG).show();
        btnAiGenerate.setEnabled(false);
        btnAiGenerate.setText("绘画中...");

        // 构建中文提示词，利用智谱优异的中文理解力
        String prompt = "专业美食摄影图，菜品名称：" + name + "，所属分类：" + category + "。";
        if (!TextUtils.isEmpty(desc)) {
            prompt += "菜品描述：" + desc + "。";
        }
        prompt += "要求：画面中心突出这道菜，令人食欲大增，高清晰度，极简桌面背景。";
        prompt = prompt.replace("\"", "\\\""); // 防止引号破坏 JSON 格式

        // 智谱 cogview-3-plus 的标准 JSON 请求体
        String json = "{"
                + "\"model\": \"cogview-3-plus\","
                + "\"prompt\": \"" + prompt + "\""
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(GLM_API_URL)
                .addHeader("Authorization", "Bearer " + GLM_API_KEY)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(DishEditActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    resetAiButton();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        // 解析 JSON 提取 url 字段
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        String imageUrl = dataArray.getJSONObject(0).getString("url");

                        runOnUiThread(() -> {
                            selectedImageUri = imageUrl; // 存入网络 URL
                            Glide.with(DishEditActivity.this)
                                    .load(imageUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .into(ivPreview);
                            Toast.makeText(DishEditActivity.this, "生成成功！", Toast.LENGTH_SHORT).show();
                            resetAiButton();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(DishEditActivity.this, "解析图片地址失败", Toast.LENGTH_SHORT).show();
                            resetAiButton();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(DishEditActivity.this, "API 请求错误: " + response.code(), Toast.LENGTH_SHORT).show();
                        resetAiButton();
                    });
                }
            }
        });
    }

    private void resetAiButton() {
        btnAiGenerate.setEnabled(true);
        btnAiGenerate.setText("✨ AI 根据描述生成图片");
    }

    // ==========================================
    // 【修改】相册选取后，将图片拷贝到 App 专属沙盒目录
    // 彻底解决用户去相册删除原图后，App 内无法显示的问题！
    // ==========================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String localPrivatePath = saveImageToInternalStorage(uri);
                if (localPrivatePath != null) {
                    selectedImageUri = localPrivatePath; // 现在保存的是 App 私有目录的绝对路径
                    Glide.with(this).load(selectedImageUri).into(ivPreview);
                }
            }
        }
    }

    // 执行拷贝，存入 /data/user/0/包名/files/ 目录下
    private String saveImageToInternalStorage(Uri uri) {
        try {
            String fileName = "dish_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);

            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "图片转存至私有目录失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveDish() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "名称、价格和分类不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        Dish dish = new Dish();
        dish.setName(name);
        dish.setPrice(Double.parseDouble(priceStr));
        dish.setCategory(category);
        dish.setImageUrl(selectedImageUri);
        dish.setDescription(etDesc.getText().toString().trim());
        dish.setIsFeatured(cbFeatured.isChecked() ? 1 : 0);
        dish.setIsSetMeal(cbSetMeal.isChecked() ? 1 : 0);

        if (isEditMode) {
            dish.setId(dishIdToEdit);
            dbHelper.updateDish(dish);
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addDish(dish);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}