package com.example.electronicsgadgets.firebase;

import android.content.Context;

import com.example.electronicsgadgets.models.NotificationModel;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.OrderItem;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.SampleData;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final Context context;
    private final SessionManager sessionManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isFirebaseAvailable = false;

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface ProductsCallback {
        void onSuccess(List<Product> products);
        void onFailure(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OrdersCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String errorMessage);
    }

    public interface NotificationsCallback {
        void onSuccess(List<NotificationModel> notifications);
        void onFailure(String errorMessage);
    }

    public FirebaseHelper(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
        try {
            if (!FirebaseApp.getApps(this.context).isEmpty()) {
                auth = FirebaseAuth.getInstance();
                db = FirebaseFirestore.getInstance();
                isFirebaseAvailable = true;
            }
        } catch (Exception e) {
            isFirebaseAvailable = false;
        }
    }

    public boolean isFirebaseAvailable() {
        return isFirebaseAvailable;
    }

    // --- Authentication ---

    public void registerUser(String name, String email, String phone, String password, AuthCallback callback) {
        // Demo Admin credentials check
        if ("admin@electronics.com".equalsIgnoreCase(email.trim()) && "admin123".equals(password)) {
            User admin = new User("admin_demo", "System Admin", email.trim(), phone.trim(), "admin", System.currentTimeMillis());
            sessionManager.createSession(admin);
            callback.onSuccess(admin);
            return;
        }

        if (!isFirebaseAvailable) {
            // Local / Demo Mode Registration
            String uid = "user_" + System.currentTimeMillis();
            User user = new User(uid, name, email, phone, "user", System.currentTimeMillis());
            sessionManager.createSession(user);
            callback.onSuccess(user);
            return;
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        User user = new User(uid, name, email, phone, "user", System.currentTimeMillis());
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    sessionManager.createSession(user);
                                    callback.onSuccess(user);
                                })
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        // Demo Admin credentials check
        if ("admin@electronics.com".equalsIgnoreCase(email.trim()) && "admin123".equals(password)) {
            User admin = new User("admin_demo", "System Admin", email.trim(), "9876543210", "admin", System.currentTimeMillis());
            sessionManager.createSession(admin);
            callback.onSuccess(admin);
            return;
        }

        if (!isFirebaseAvailable) {
            // Local / Demo Mode Login
            User user = new User("user_demo", "Demo Customer", email.trim(), "9876543210", "user", System.currentTimeMillis());
            sessionManager.createSession(user);
            callback.onSuccess(user);
            return;
        }

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User user = documentSnapshot.toObject(User.class);
                                        if (user != null) {
                                            sessionManager.createSession(user);
                                            callback.onSuccess(user);
                                            return;
                                        }
                                    }
                                    // Fallback if doc doesn't exist
                                    User user = new User(uid, "Customer", email.trim(), "", "user", System.currentTimeMillis());
                                    sessionManager.createSession(user);
                                    callback.onSuccess(user);
                                })
                                .addOnFailureListener(e -> {
                                    User user = new User(uid, "Customer", email.trim(), "", "user", System.currentTimeMillis());
                                    sessionManager.createSession(user);
                                    callback.onSuccess(user);
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void sendPasswordResetEmail(String email, ActionCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess();
            return;
        }
        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- Product Management ---

    public void getAllProducts(ProductsCallback callback) {
        fetchProducts(callback);
    }

    public void fetchProducts(ProductsCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess(sessionManager.getLocalProducts());
            return;
        }

        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> firestoreProducts = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            if (p.getProductId() == null || p.getProductId().isEmpty()) {
                                p.setProductId(doc.getId());
                            }
                            firestoreProducts.add(p);
                        }
                    }

                    // Seed SampleData into Firestore ONLY when collection is completely empty
                    if (queryDocumentSnapshots.isEmpty()) {
                        List<Product> sampleProducts = SampleData.getSampleProducts();
                        for (Product sp : sampleProducts) {
                            db.collection("products").document(sp.getProductId()).set(sp);
                            firestoreProducts.add(sp);
                        }
                    }

                    sessionManager.saveLocalProducts(firestoreProducts);
                    callback.onSuccess(firestoreProducts);
                })
                .addOnFailureListener(e -> callback.onSuccess(sessionManager.getLocalProducts()));
    }

    public void createProduct(Product product, ActionCallback callback) {
        if (product == null) {
            if (callback != null) callback.onFailure("Product cannot be null");
            return;
        }

        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            String docId = isFirebaseAvailable
                    ? db.collection("products").document().getId()
                    : "prod_" + System.currentTimeMillis();
            product.setProductId(docId);
        }

        sessionManager.addLocalProduct(product);

        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        db.collection("products").document(product.getProductId()).set(product)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateProduct(Product product, ActionCallback callback) {
        if (product == null || product.getProductId() == null || product.getProductId().isEmpty()) {
            if (callback != null) callback.onFailure("Product ID missing for update");
            return;
        }

        sessionManager.updateLocalProduct(product);

        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        db.collection("products").document(product.getProductId()).set(product)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void saveProduct(Product product, ActionCallback callback) {
        if (product.getProductId() != null && !product.getProductId().isEmpty()) {
            List<Product> localList = sessionManager.getLocalProducts();
            boolean exists = false;
            for (Product p : localList) {
                if (product.getProductId().equals(p.getProductId())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                updateProduct(product, callback);
                return;
            }
        }
        createProduct(product, callback);
    }

    public void deleteProduct(String productId, ActionCallback callback) {
        if (productId == null || productId.isEmpty()) {
            if (callback != null) callback.onFailure("Product ID is required");
            return;
        }

        sessionManager.deleteLocalProduct(productId);

        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        db.collection("products").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // --- Notification Management ---

    public void saveNotification(NotificationModel notification, ActionCallback callback) {
        sessionManager.saveLocalNotification(notification);
        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        String docId = notification.getNotificationId() != null && !notification.getNotificationId().isEmpty()
                ? notification.getNotificationId()
                : db.collection("notifications").document().getId();
        notification.setNotificationId(docId);

        db.collection("notifications").document(docId).set(notification)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSuccess();
                });
    }

    public void fetchNotifications(String userId, NotificationsCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess(sessionManager.getLocalNotificationsForUser(userId));
            return;
        }

        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        NotificationModel n = doc.toObject(NotificationModel.class);
                        if (n != null) {
                            if (n.getUserId() == null || n.getUserId().isEmpty() || "all".equalsIgnoreCase(n.getUserId()) || n.getUserId().equalsIgnoreCase(userId)) {
                                list.add(n);
                            }
                        }
                    }
                    if (list.isEmpty()) {
                        callback.onSuccess(sessionManager.getLocalNotificationsForUser(userId));
                    } else {
                        list.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                        callback.onSuccess(list);
                    }
                })
                .addOnFailureListener(e -> callback.onSuccess(sessionManager.getLocalNotificationsForUser(userId)));
    }

    public void markNotificationAsRead(String notificationId, ActionCallback callback) {
        sessionManager.markNotificationAsRead(notificationId);
        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        db.collection("notifications").document(notificationId).update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSuccess();
                });
    }

    public void markAllNotificationsAsRead(String userId, ActionCallback callback) {
        sessionManager.markAllNotificationsAsRead(userId);
        if (!isFirebaseAvailable) {
            if (callback != null) callback.onSuccess();
            return;
        }

        db.collection("notifications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        NotificationModel n = doc.toObject(NotificationModel.class);
                        if (n != null && (n.getUserId() == null || n.getUserId().isEmpty() || "all".equalsIgnoreCase(n.getUserId()) || n.getUserId().equalsIgnoreCase(userId))) {
                            doc.getReference().update("isRead", true);
                        }
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSuccess();
                });
    }

    // --- Order Management ---

    public void placeOrder(Order order, ActionCallback callback) {
        if (order != null && order.getItems() != null) {
            List<Product> localProducts = sessionManager.getLocalProducts();
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() != null) {
                    for (Product p : localProducts) {
                        if (item.getProductId().equals(p.getProductId())) {
                            int newStock = Math.max(0, p.getStock() - item.getQuantity());
                            p.setStock(newStock);
                            sessionManager.updateLocalProduct(p);

                            if (isFirebaseAvailable) {
                                db.collection("products").document(p.getProductId()).update("stock", newStock);
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (!isFirebaseAvailable) {
            sessionManager.saveLocalOrder(order);
            sessionManager.clearCart();
            callback.onSuccess();
            return;
        }

        String docId = order.getOrderId() != null && !order.getOrderId().isEmpty()
                ? order.getOrderId()
                : db.collection("orders").document().getId();
        order.setOrderId(docId);

        db.collection("orders").document(docId).set(order)
                .addOnSuccessListener(aVoid -> {
                    sessionManager.saveLocalOrder(order);
                    sessionManager.clearCart();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Fallback to local save
                    sessionManager.saveLocalOrder(order);
                    sessionManager.clearCart();
                    callback.onSuccess();
                });
    }

    public void fetchUserOrders(String userId, OrdersCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess(sessionManager.getLocalOrders());
            return;
        }

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) orders.add(o);
                    }
                    if (orders.isEmpty()) {
                        callback.onSuccess(sessionManager.getLocalOrders());
                    } else {
                        callback.onSuccess(orders);
                    }
                })
                .addOnFailureListener(e -> callback.onSuccess(sessionManager.getLocalOrders()));
    }

    public void fetchAllOrders(OrdersCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess(sessionManager.getLocalOrders());
            return;
        }

        db.collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) orders.add(o);
                    }
                    if (orders.isEmpty()) {
                        callback.onSuccess(sessionManager.getLocalOrders());
                    } else {
                        callback.onSuccess(orders);
                    }
                })
                .addOnFailureListener(e -> callback.onSuccess(sessionManager.getLocalOrders()));
    }

    public void updateOrderStatus(String orderId, String newStatus, ActionCallback callback) {
        sessionManager.updateLocalOrderStatus(orderId, newStatus);
        if (!isFirebaseAvailable) {
            callback.onSuccess();
            return;
        }

        db.collection("orders").document(orderId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onSuccess()); // Local updated already
    }
}
