package com.example.electronicsgadgets.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.NotificationModel;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.OrderItem;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final String PREF_NAME = "ElectronicsGadgetsSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_CART_ITEMS = "cartItemsJson";
    private static final String KEY_ORDERS = "ordersJson";
    private static final String KEY_CUSTOM_PRODUCTS = "customProductsJson";
    private static final String KEY_NOTIFICATIONS = "notificationsJson";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUid());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUserDetails() {
        if (!isLoggedIn()) return null;
        return new User(
                pref.getString(KEY_USER_ID, "user_demo"),
                pref.getString(KEY_USER_NAME, "Demo Customer"),
                pref.getString(KEY_USER_EMAIL, "user@electronics.com"),
                pref.getString(KEY_USER_PHONE, "9876543210"),
                pref.getString(KEY_USER_ROLE, "user"),
                System.currentTimeMillis()
        );
    }

    public void updateProfile(String name, String phone) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    // --- Local Cart Management ---
    public void saveCart(List<CartItem> cartItems) {
        try {
            JSONArray array = new JSONArray();
            for (CartItem item : cartItems) {
                JSONObject obj = new JSONObject();
                obj.put("cartItemId", item.getCartItemId());
                obj.put("productId", item.getProductId());
                obj.put("productName", item.getProductName());
                obj.put("productBrand", item.getProductBrand());
                obj.put("price", item.getPrice());
                obj.put("imageUrl", item.getImageUrl());
                obj.put("quantity", item.getQuantity());
                obj.put("stockQuantity", item.getStockQuantity());
                array.put(obj);
            }
            editor.putString(KEY_CART_ITEMS, array.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CartItem> getCart() {
        List<CartItem> list = new ArrayList<>();
        String json = pref.getString(KEY_CART_ITEMS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                CartItem item = new CartItem(
                        obj.optString("cartItemId"),
                        obj.optString("productId"),
                        obj.optString("productName"),
                        obj.optString("productBrand"),
                        obj.optDouble("price"),
                        obj.optString("imageUrl"),
                        obj.optInt("quantity"),
                        obj.optInt("stockQuantity")
                );
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clearCart() {
        editor.remove(KEY_CART_ITEMS);
        editor.apply();
    }

    // --- Local Orders Management ---
    public void saveLocalOrder(Order order) {
        List<Order> orders = getLocalOrders();
        orders.add(0, order); // Most recent first
        try {
            JSONArray array = new JSONArray();
            for (Order o : orders) {
                JSONObject obj = new JSONObject();
                obj.put("orderId", o.getOrderId());
                obj.put("userId", o.getUserId());
                obj.put("customerName", o.getCustomerName());
                obj.put("customerPhone", o.getCustomerPhone());
                obj.put("address", o.getAddress());
                obj.put("city", o.getCity());
                obj.put("state", o.getState());
                obj.put("pinCode", o.getPinCode());
                obj.put("subtotal", o.getSubtotal());
                obj.put("deliveryFee", o.getDeliveryFee());
                obj.put("totalAmount", o.getTotalAmount());
                obj.put("orderDate", o.getOrderDate());
                obj.put("status", o.getStatus());

                JSONArray itemsArr = new JSONArray();
                if (o.getItems() != null) {
                    for (OrderItem item : o.getItems()) {
                        JSONObject io = new JSONObject();
                        io.put("productId", item.getProductId());
                        io.put("productName", item.getProductName());
                        io.put("price", item.getPrice());
                        io.put("quantity", item.getQuantity());
                        io.put("imageUrl", item.getImageUrl());
                        itemsArr.put(io);
                    }
                }
                obj.put("items", itemsArr);
                array.put(obj);
            }
            editor.putString(KEY_ORDERS, array.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> getLocalOrders() {
        List<Order> list = new ArrayList<>();
        String json = pref.getString(KEY_ORDERS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JSONArray itemsArr = obj.optJSONArray("items");
                List<OrderItem> items = new ArrayList<>();
                if (itemsArr != null) {
                    for (int j = 0; j < itemsArr.length(); j++) {
                        JSONObject io = itemsArr.getJSONObject(j);
                        items.add(new OrderItem(
                                io.optString("productId"),
                                io.optString("productName"),
                                io.optDouble("price"),
                                io.optInt("quantity"),
                                io.optString("imageUrl")
                        ));
                    }
                }

                Order order = new Order(
                        obj.optString("orderId"),
                        obj.optString("userId"),
                        obj.optString("customerName"),
                        obj.optString("customerPhone"),
                        obj.optString("address"),
                        obj.optString("city"),
                        obj.optString("state"),
                        obj.optString("pinCode"),
                        items,
                        obj.optDouble("subtotal"),
                        obj.optDouble("deliveryFee"),
                        obj.optDouble("totalAmount"),
                        obj.optString("orderDate"),
                        obj.optString("status")
                );
                list.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateLocalOrderStatus(String orderId, String newStatus) {
        List<Order> orders = getLocalOrders();
        for (Order o : orders) {
            if (o.getOrderId().equals(orderId)) {
                o.setStatus(newStatus);
                break;
            }
        }
        try {
            JSONArray array = new JSONArray();
            for (Order o : orders) {
                JSONObject obj = new JSONObject();
                obj.put("orderId", o.getOrderId());
                obj.put("userId", o.getUserId());
                obj.put("customerName", o.getCustomerName());
                obj.put("customerPhone", o.getCustomerPhone());
                obj.put("address", o.getAddress());
                obj.put("city", o.getCity());
                obj.put("state", o.getState());
                obj.put("pinCode", o.getPinCode());
                obj.put("subtotal", o.getSubtotal());
                obj.put("deliveryFee", o.getDeliveryFee());
                obj.put("totalAmount", o.getTotalAmount());
                obj.put("orderDate", o.getOrderDate());
                obj.put("status", o.getStatus());

                JSONArray itemsArr = new JSONArray();
                if (o.getItems() != null) {
                    for (OrderItem item : o.getItems()) {
                        JSONObject io = new JSONObject();
                        io.put("productId", item.getProductId());
                        io.put("productName", item.getProductName());
                        io.put("price", item.getPrice());
                        io.put("quantity", item.getQuantity());
                        io.put("imageUrl", item.getImageUrl());
                        itemsArr.put(io);
                    }
                }
                obj.put("items", itemsArr);
                array.put(obj);
            }
            editor.putString(KEY_ORDERS, array.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Custom Products Management (Offline Add/Edit/Delete) ---
    public void saveLocalProducts(List<Product> products) {
        try {
            JSONArray array = new JSONArray();
            if (products != null) {
                for (Product p : products) {
                    JSONObject obj = new JSONObject();
                    obj.put("productId", p.getProductId());
                    obj.put("name", p.getName());
                    obj.put("brand", p.getBrand());
                    obj.put("category", p.getCategory());
                    obj.put("price", p.getPrice());
                    obj.put("originalPrice", p.getOriginalPrice());
                    obj.put("discount", p.getDiscount());
                    obj.put("rating", (double) p.getRating());
                    obj.put("description", p.getDescription());
                    obj.put("specifications", p.getSpecifications());
                    obj.put("imageUrl", p.getImageUrl());
                    obj.put("stock", p.getStock());
                    obj.put("isFeatured", p.isFeatured());
                    array.put(obj);
                }
            }
            editor.putString(KEY_CUSTOM_PRODUCTS, array.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAllProducts(List<Product> products) {
        saveLocalProducts(products);
    }

    public List<Product> getLocalProducts() {
        String json = pref.getString(KEY_CUSTOM_PRODUCTS, null);
        if (json == null) {
            List<Product> initial = SampleData.getSampleProducts();
            saveLocalProducts(initial);
            return initial;
        }
        List<Product> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Product p = new Product(
                        obj.optString("productId"),
                        obj.optString("name"),
                        obj.optString("brand"),
                        obj.optString("category"),
                        obj.optDouble("price"),
                        obj.optDouble("originalPrice"),
                        obj.optInt("discount"),
                        (float) obj.optDouble("rating", 4.5),
                        obj.optString("description"),
                        obj.optString("specifications"),
                        obj.optString("imageUrl"),
                        obj.optInt("stock"),
                        obj.optBoolean("isFeatured")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SampleData.getSampleProducts();
        }
        return list;
    }

    public void addLocalProduct(Product product) {
        if (product == null) return;
        List<Product> list = getLocalProducts();
        boolean updated = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getProductId() != null && list.get(i).getProductId().equals(product.getProductId())) {
                list.set(i, product);
                updated = true;
                break;
            }
        }
        if (!updated) {
            list.add(product);
        }
        saveLocalProducts(list);
    }

    public void updateLocalProduct(Product product) {
        if (product == null || product.getProductId() == null) return;
        List<Product> list = getLocalProducts();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getProductId() != null && list.get(i).getProductId().equals(product.getProductId())) {
                list.set(i, product);
                break;
            }
        }
        saveLocalProducts(list);
    }

    public void deleteLocalProduct(String productId) {
        if (productId == null) return;
        List<Product> list = getLocalProducts();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getProductId() != null && list.get(i).getProductId().equals(productId)) {
                list.remove(i);
                break;
            }
        }
        saveLocalProducts(list);
    }

    // --- Notifications Management ---

    public void saveLocalNotification(NotificationModel notification) {
        List<NotificationModel> list = getAllLocalNotifications();
        // Insert most recent first
        list.add(0, notification);
        saveAllLocalNotifications(list);
    }

    private void saveAllLocalNotifications(List<NotificationModel> notifications) {
        try {
            JSONArray array = new JSONArray();
            for (NotificationModel n : notifications) {
                JSONObject obj = new JSONObject();
                obj.put("notificationId", n.getNotificationId());
                obj.put("userId", n.getUserId());
                obj.put("title", n.getTitle());
                obj.put("message", n.getMessage());
                obj.put("type", n.getType());
                obj.put("orderId", n.getOrderId());
                obj.put("productId", n.getProductId());
                obj.put("timestamp", n.getTimestamp());
                obj.put("isRead", n.isRead());
                array.put(obj);
            }
            editor.putString(KEY_NOTIFICATIONS, array.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<NotificationModel> getAllLocalNotifications() {
        List<NotificationModel> list = new ArrayList<>();
        String json = pref.getString(KEY_NOTIFICATIONS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                NotificationModel n = new NotificationModel(
                        obj.optString("notificationId"),
                        obj.optString("userId"),
                        obj.optString("title"),
                        obj.optString("message"),
                        obj.optString("type"),
                        obj.optString("orderId"),
                        obj.optString("productId"),
                        obj.optLong("timestamp"),
                        obj.optBoolean("isRead")
                );
                list.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<NotificationModel> getLocalNotificationsForUser(String userId) {
        List<NotificationModel> all = getAllLocalNotifications();
        List<NotificationModel> filtered = new ArrayList<>();
        for (NotificationModel n : all) {
            if (n.getUserId() == null || n.getUserId().isEmpty() || "all".equalsIgnoreCase(n.getUserId()) || n.getUserId().equalsIgnoreCase(userId)) {
                filtered.add(n);
            }
        }
        return filtered;
    }

    public void markNotificationAsRead(String notificationId) {
        List<NotificationModel> all = getAllLocalNotifications();
        boolean changed = false;
        for (NotificationModel n : all) {
            if (n.getNotificationId() != null && n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                changed = true;
                break;
            }
        }
        if (changed) {
            saveAllLocalNotifications(all);
        }
    }

    public void markAllNotificationsAsRead(String userId) {
        List<NotificationModel> all = getAllLocalNotifications();
        boolean changed = false;
        for (NotificationModel n : all) {
            if (n.getUserId() == null || n.getUserId().isEmpty() || "all".equalsIgnoreCase(n.getUserId()) || n.getUserId().equalsIgnoreCase(userId)) {
                if (!n.isRead()) {
                    n.setRead(true);
                    changed = true;
                }
            }
        }
        if (changed) {
            saveAllLocalNotifications(all);
        }
    }

    public int getUnreadNotificationCount(String userId) {
        List<NotificationModel> userNotifications = getLocalNotificationsForUser(userId);
        int unread = 0;
        for (NotificationModel n : userNotifications) {
            if (!n.isRead()) {
                unread++;
            }
        }
        return unread;
    }

    public boolean hasOrderNotification(String orderId, String type, String userId) {
        List<NotificationModel> all = getAllLocalNotifications();
        for (NotificationModel n : all) {
            if (orderId != null && orderId.equals(n.getOrderId()) && type != null && type.equals(n.getType())) {
                if (userId == null || userId.equals(n.getUserId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
