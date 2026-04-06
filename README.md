# 🍽️ 智能点餐系统 (DealOrder)

基于 Android 原生 (Java) 开发的轻量级智能点餐系统。该应用不仅提供了完整的用户端点餐流程（浏览、加购、结算、历史订单），还内置了隐藏的“商家管理”功能，最大亮点是**接入了智谱大模型 (CogView) API**，支持通过文本描述一键生成精美的菜品展示图。

---

## ✨ 核心功能特性

### 📱 用户端功能
* **首页 (Home)**
  * **特色推荐轮播图**：使用 Banner 动态展示商家主推菜品。
  * **动态分类标签**：根据数据库真实菜品数据，动态生成菜品分类 Tab（如川菜、粤菜、主食等）。
  * **超值套餐**：单独的套餐展示区域，支持一键加购。
* **购物车 (Cart)**
  * 支持修改菜品数量、单品删除与一键清空。
  * **智能结算系统**：支持叠加使用“满减券”和“折扣券”，实时计算优惠金额与实付总价。
* **订单管理 (Order)**
  * 填写订单备注，一键提交生成订单。
  * 历史订单流展示，包含订单时间、购买明细及最终实付款。
* **个人中心 (Mine)**
  * **会员系统**：消费自动累计会员积分。
  * **优惠券管理**：查看当前账号下可用的优惠券明细。

### 👨‍🍳 商家端功能 (内置管理)
* **菜品管理**：长按首页菜品可进入编辑/删除模式，支持修改菜品名称、价格、分类和套餐属性。
* **✨ AI 菜品图生成 (核心亮点)**：
  * 在添加/修改菜品时，如果没有合适的实拍图，可输入菜品名称和描述，点击 **“✨ AI 根据描述生成图片”**。
  * 系统底层调用 **智谱大模型 (cogview-3-plus)**，自动绘制高精度的美食摄影图。
  * 生成的图片会自动下载并转存至 App 的**私有沙盒目录**，彻底解决相册授权和图片被误删的问题。
* **系统重置**：首页底部提供“重置系统”按钮，一键恢复初始数据库，方便演示与测试。

---

## 🛠️ 技术栈

* **开发语言**：Java
* **UI 架构**：`ViewPager2` + `BottomNavigationView` + `Fragment` (主流底部导航架构)
* **数据存储**：
  * **SQLite (SQLiteOpenHelper)**：本地数据库，多表关联（菜品表、订单表、订单详情表、优惠券表、用户表）。
  * **App Private Storage**：使用私有沙盒存储下载的 AI 图片，保证应用数据独立性。
* **第三方开源库**：
  * **Glide**：高性能图片加载（支持网络URL与本地资源混合加载）。
  * **Youth Banner**：首页轮播图组件。
  * **OkHttp3**：处理与大模型的网络请求。

---

## 📂 项目结构概览

```text
com.example.dealorder
│
├── activity/               # 页面级容器
│   ├── SplashActivity      # 启动页 (倒计时跳转)
│   ├── MainActivity        # 主页面 (承载四个Fragment)
│   ├── OrderConfirmActivity# 确认订单结算页
│   └── DishEditActivity    # 菜品添加/编辑页 (含AI绘图逻辑)
│
├── fragment/               # 核心业务模块
│   ├── HomeFragment        # 首页模块
│   ├── CartFragment        # 购物车模块
│   ├── OrderFragment       # 订单历史模块
│   └── MineFragment        # 我的/个人中心模块
│
├── adapter/                # RecyclerView/Banner 适配器
│   ├── DishAdapter         # 菜品列表适配器
│   ├── CartAdapter         # 购物车列表适配器
│   ├── OrderAdapter        # 订单列表适配器
│   └── FeaturedAdapter     # 轮播图适配器
│
├── manager/                # 业务管理类
│   └── CartManager         # 购物车全局单例管理 (内存缓存)
│
├── entity/                 # 实体类 (POJO)
│   ├── Dish, Order, Coupon
│
└── db/                     # 数据库管理
    └── DBHelper            # SQLite 帮助类及 SQL 语句
```

---

## 🚀 运行与配置指南

### 1. 基础运行
项目可以直接在 Android Studio 中打开并运行。首次启动时，`DBHelper` 会自动执行 `initTestData`，为您预置丰富的测试数据（包含川菜、粤菜、主食及3张测试优惠券）。

### 2. 配置 AI 绘图功能 (必看)
为了使 `DishEditActivity` 中的 **AI 图片生成** 功能正常工作，您需要配置智谱开放平台的 API Key。

1. 前往 [智谱大模型开放平台](https://open.bigmodel.cn/) 注册并获取 API Key。
2. 打开 `app/src/main/java/com/example/dealorder/activity/DishEditActivity.java`。
3. 找到以下常量，并将您的 Key 填入：
   ```java
   // 请在此处填入你在智谱开放平台申请的 API Key
   private static final String GLM_API_KEY = "你的_API_KEY_在这里";
   ```

### 3. 权限说明
本项目已在 `AndroidManifest.xml` 中声明了以下必要权限：
* `<uses-permission android:name="android.permission.INTERNET" />`：用于请求 AI 图片接口和加载网络图片。

---

## 💡 使用小贴士
* **如何触发商家功能？**
  * 在 **“我的”** 页面点击“【商家功能】添加新菜品”。
  * 在 **首页** 长按任意普通菜品，即可进入菜品编辑/删除模式。
* **积分怎么来？**
  * 每次成功提交订单后，实付金额将按 `1:1` 比例自动转化为会员积分，并在“我的”页面展示。

---
*Developed with ❤️ for Android.*