package com.example.electronicsgadgets.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.NotificationModel;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;

public class NotificationHelper {

    public static final String CHANNEL_ID = "electronics_gadgets_notifications";
    public static final String CHANNEL_NAME = "Electronics & Gadgets";

    public static final String EXTRA_TARGET_FRAGMENT = "extra_target_fragment";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for order updates, cart items, and offers.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, NotificationModel notification) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String targetFragment = "HOME";
        if ("cart".equalsIgnoreCase(notification.getType())) {
            targetFragment = "CART";
        } else if ("order".equalsIgnoreCase(notification.getType())) {
            targetFragment = "ORDERS";
        } else if ("product".equalsIgnoreCase(notification.getType())) {
            targetFragment = "PRODUCTS";
        } else if ("offer".equalsIgnoreCase(notification.getType())) {
            targetFragment = "HOME";
        }

        intent.putExtra(EXTRA_TARGET_FRAGMENT, targetFragment);
        if (notification.getOrderId() != null) {
            intent.putExtra(EXTRA_ORDER_ID, notification.getOrderId());
        }
        if (notification.getProductId() != null) {
            intent.putExtra(EXTRA_PRODUCT_ID, notification.getProductId());
        }
        if (notification.getNotificationId() != null) {
            intent.putExtra(EXTRA_NOTIFICATION_ID, notification.getNotificationId());
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        int notificationId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, skip system notification dispatch but persistent notification is saved.
                return;
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helper Triggers ---

    public static void notifyAddToCart(Context context, Product product) {
        SessionManager sessionManager = new SessionManager(context);
        User currentUser = sessionManager.getUserDetails();
        String userId = currentUser != null ? currentUser.getUid() : "user_demo";

        String notificationId = "notif_" + System.currentTimeMillis();
        String title = "Added to Cart 🛒";
        String message = product.getName() + " has been added to your cart.";

        NotificationModel model = new NotificationModel(
                notificationId,
                userId,
                title,
                message,
                "cart",
                null,
                product.getProductId(),
                System.currentTimeMillis(),
                false
        );

        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.saveNotification(model, null);
        showNotification(context, model);
    }

    public static void notifyOrderPlaced(Context context, Order order) {
        SessionManager sessionManager = new SessionManager(context);
        User currentUser = sessionManager.getUserDetails();
        String userId = order.getUserId() != null ? order.getUserId() : (currentUser != null ? currentUser.getUid() : "user_demo");

        String notificationId = "notif_" + System.currentTimeMillis();
        String title = "Order Placed Successfully 🎉";
        String message = "Your order #" + order.getOrderId() + " has been placed successfully.";

        NotificationModel model = new NotificationModel(
                notificationId,
                userId,
                title,
                message,
                "order",
                order.getOrderId(),
                null,
                System.currentTimeMillis(),
                false
        );

        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.saveNotification(model, null);
        showNotification(context, model);
    }

    public static void notifyOrderStatusChanged(Context context, Order order, String newStatus) {
        SessionManager sessionManager = new SessionManager(context);
        String userId = order.getUserId() != null ? order.getUserId() : "user_demo";

        // De-duplication check
        if (sessionManager.hasOrderNotification(order.getOrderId(), newStatus, userId)) {
            return;
        }

        String title;
        String message;

        if ("Confirmed".equalsIgnoreCase(newStatus)) {
            title = "Order Confirmed ✅";
            message = "Your order #" + order.getOrderId() + " has been confirmed.";
        } else if ("Shipped".equalsIgnoreCase(newStatus)) {
            title = "Your Order Has Shipped 🚚";
            message = "Your order #" + order.getOrderId() + " is on the way.";
        } else if ("Delivered".equalsIgnoreCase(newStatus)) {
            title = "Order Delivered 📦";
            message = "Your order #" + order.getOrderId() + " has been delivered successfully.";
        } else if ("Cancelled".equalsIgnoreCase(newStatus)) {
            title = "Order Cancelled";
            message = "Your order #" + order.getOrderId() + " has been cancelled.";
        } else {
            title = "Order Status Updated";
            message = "Your order #" + order.getOrderId() + " status is now " + newStatus + ".";
        }

        String notificationId = "notif_" + System.currentTimeMillis();
        NotificationModel model = new NotificationModel(
                notificationId,
                userId,
                title,
                message,
                "order",
                order.getOrderId(),
                null,
                System.currentTimeMillis(),
                false
        );

        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.saveNotification(model, null);
        showNotification(context, model);
    }

    public static void notifyNewProduct(Context context, Product product) {
        String notificationId = "notif_" + System.currentTimeMillis();
        String title = "New Product Available ✨";
        String message = product.getName() + " is now available in Electronics & Gadgets.";

        NotificationModel model = new NotificationModel(
                notificationId,
                "all", // broadcast to all users
                title,
                message,
                "product",
                null,
                product.getProductId(),
                System.currentTimeMillis(),
                false
        );

        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.saveNotification(model, null);
        showNotification(context, model);
    }

    public static void notifySpecialOffer(Context context, String title, String message) {
        String notificationId = "notif_" + System.currentTimeMillis();
        String notifTitle = title != null && !title.isEmpty() ? title : "Special Offer 🔥";
        String notifMsg = message != null && !message.isEmpty() ? message : "Get amazing deals on Electronics & Gadgets today.";

        NotificationModel model = new NotificationModel(
                notificationId,
                "all",
                notifTitle,
                notifMsg,
                "offer",
                null,
                null,
                System.currentTimeMillis(),
                false
        );

        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.saveNotification(model, null);
        showNotification(context, model);
    }
}
