package com.example.electronicsgadgets.utils;

import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.models.Category;
import com.example.electronicsgadgets.models.Product;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("cat_all", "All", R.drawable.ic_products));
        categories.add(new Category("cat_smartphones", "Smartphones", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_laptops", "Laptops", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_tablets", "Tablets", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_headphones", "Headphones", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_watches", "Smart Watches", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_cameras", "Cameras", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_gaming", "Gaming", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_monitors", "Monitors & Drones", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_smarthome", "Smart Home", R.drawable.ic_electronics_logo));
        categories.add(new Category("cat_accessories", "Accessories", R.drawable.ic_electronics_logo));
        return categories;
    }

    public static List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();

        // Smartphones
        products.add(new Product(
                "prod_1",
                "Galaxy Ultra S24",
                "Samsung",
                "Smartphones",
                1199.99,
                1399.99,
                14,
                4.8f,
                "Experience peak mobile innovation with Galaxy AI, a titanium frame, 200MP camera system, and all-day battery life.",
                "Display: 6.8\" Dynamic AMOLED 2X\nProcessor: Snapdragon 8 Gen 3\nRAM: 12GB\nStorage: 512GB\nBattery: 5000mAh",
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500",
                25,
                true
        ));

        products.add(new Product(
                "prod_2",
                "iPhone 15 Pro Max",
                "Apple",
                "Smartphones",
                1299.99,
                1449.99,
                10,
                4.9f,
                "Aerospace-grade titanium design, A17 Pro chip, customizable Action button, and 5x Telephoto camera lens.",
                "Display: 6.7\" Super Retina XDR\nProcessor: A17 Pro Chip\nRAM: 8GB\nStorage: 256GB\nCamera: 48MP Main System",
                "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=500",
                18,
                true
        ));

        products.add(new Product(
                "prod_3",
                "Pixel 8 Pro",
                "Google",
                "Smartphones",
                899.99,
                999.99,
                10,
                4.7f,
                "Advanced AI mobile processor with pro-level tri-camera setup and Super Actua OLED screen display.",
                "Display: 6.7\" OLED 120Hz\nProcessor: Google Tensor G3\nRAM: 12GB\nStorage: 128GB",
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500",
                30,
                false
        ));

        products.add(new Product(
                "prod_16",
                "OnePlus 12 5G",
                "OnePlus",
                "Smartphones",
                799.99,
                899.99,
                11,
                4.7f,
                "Smooth Beyond Belief with 4th Gen Hasselblad Camera System, Trinity Engine performance, and 100W SUPERVOOC charging.",
                "Display: 6.82\" QHD+ 120Hz LTPO OLED\nProcessor: Snapdragon 8 Gen 3\nRAM: 16GB\nStorage: 512GB",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500",
                20,
                true
        ));

        // Laptops
        products.add(new Product(
                "prod_4",
                "MacBook Pro 16\" M3 Max",
                "Apple",
                "Laptops",
                2499.99,
                2799.99,
                11,
                4.9f,
                "The ultimate pro laptop featuring Liquid Retina XDR display, up to 22 hours battery life, and M3 Max silicon.",
                "Display: 16.2\" Liquid Retina XDR\nProcessor: Apple M3 Max (14-Core CPU)\nRAM: 36GB Unified\nStorage: 1TB SSD",
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500",
                12,
                true
        ));

        products.add(new Product(
                "prod_5",
                "Dell XPS 15 OLED",
                "Dell",
                "Laptops",
                1799.99,
                1999.99,
                10,
                4.6f,
                "Precision crafted aluminum body with a stunning 3.5K OLED touch screen and discrete NVIDIA RTX graphics.",
                "Display: 15.6\" 3.5K OLED Touch\nProcessor: Intel Core i9-13900H\nGPU: RTX 4060 8GB\nRAM: 32GB DDR5\nStorage: 1TB SSD",
                "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500",
                15,
                false
        ));

        products.add(new Product(
                "prod_6",
                "ROG Zephyrus G16",
                "ASUS",
                "Laptops",
                1999.99,
                2299.99,
                13,
                4.8f,
                "Ultra-slim gaming powerhouse equipped with Nebula OLED display and AI-accelerated GeForce RTX 4080 graphics.",
                "Display: 16\" 2.5K 240Hz OLED\nProcessor: Intel Core Ultra 9\nGPU: RTX 4080 12GB\nRAM: 32GB LPDDR5X",
                "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500",
                10,
                true
        ));

        products.add(new Product(
                "prod_17",
                "ThinkPad X1 Carbon Gen 12",
                "Lenovo",
                "Laptops",
                1649.99,
                1899.99,
                13,
                4.8f,
                "Ultralight carbon-fiber chassis engineered for professional mobility with Communications Bar and Intel Core Ultra processors.",
                "Display: 14\" 2.8K OLED 120Hz\nProcessor: Intel Core Ultra 7 155H\nRAM: 32GB LPDDR5X\nStorage: 1TB PCIe Gen4 SSD",
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500",
                16,
                false
        ));

        // Tablets
        products.add(new Product(
                "prod_7",
                "iPad Pro 12.9\" M2",
                "Apple",
                "Tablets",
                1099.99,
                1199.99,
                8,
                4.8f,
                "Astonishing performance with Liquid Retina XDR screen, Apple Pencil hover feature, and Thunderbolt connectivity.",
                "Display: 12.9\" Liquid Retina XDR\nProcessor: Apple M2\nRAM: 8GB\nStorage: 256GB WiFi",
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500",
                20,
                false
        ));

        products.add(new Product(
                "prod_8",
                "Galaxy Tab S9 Ultra",
                "Samsung",
                "Tablets",
                1199.99,
                1349.99,
                11,
                4.7f,
                "Massive 14.6\" Dynamic AMOLED 2X screen with included S Pen and IP68 water & dust resistance rating.",
                "Display: 14.6\" AMOLED 120Hz\nProcessor: Snapdragon 8 Gen 2\nRAM: 12GB\nStorage: 512GB",
                "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=500",
                14,
                false
        ));

        // Headphones
        products.add(new Product(
                "prod_9",
                "WH-1000XM5 Wireless Headphones",
                "Sony",
                "Headphones",
                349.99,
                399.99,
                12,
                4.9f,
                "Industry-leading noise canceling with two processors and 8 microphones for unparalleled crystal-clear calling and audio.",
                "Battery: 30 Hours Playtime\nFeatures: Active Noise Cancellation, Touch Control, Hi-Res Audio Wireless",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                40,
                true
        ));

        products.add(new Product(
                "prod_10",
                "AirPods Max",
                "Apple",
                "Headphones",
                479.99,
                549.99,
                13,
                4.6f,
                "High-fidelity audio, Active Noise Cancellation with Transparency mode, and spatial audio for theater-like sound.",
                "Drivers: Apple-designed dynamic driver\nBattery: 20 Hours with ANC\nWeight: 384.8 grams",
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500",
                25,
                false
        ));

        products.add(new Product(
                "prod_18",
                "Bose QuietComfort Ultra",
                "Bose",
                "Headphones",
                429.99,
                479.99,
                10,
                4.8f,
                "Breakthrough spatialized audio for more immersive listening, world-class noise cancellation, and CustomTune technology.",
                "Battery: 24 Hours\nModes: Quiet, Aware, Immersion\nConnectivity: Bluetooth 5.3",
                "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=500",
                22,
                true
        ));

        // Smart Watches
        products.add(new Product(
                "prod_11",
                "Apple Watch Ultra 2",
                "Apple",
                "Smart Watches",
                749.99,
                799.99,
                6,
                4.9f,
                "Rugged titanium case, dual-frequency GPS, up to 36 hours battery life, and ultra-bright 3000 nits display.",
                "Case: 49mm Titanium\nDisplay: 3000 nits Always-On Retina\nSensors: ECG, Blood Oxygen, Depth Gauge",
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500",
                18,
                true
        ));

        products.add(new Product(
                "prod_12",
                "Galaxy Watch 6 Classic",
                "Samsung",
                "Smart Watches",
                369.99,
                429.99,
                14,
                4.6f,
                "Iconic rotating bezel design, advanced sleep tracking, personalized HR zones, and BIA body composition analysis.",
                "Case: 47mm Stainless Steel\nScreen: Sapphire Crystal Glass\nOS: Wear OS Powered by Samsung",
                "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500",
                30,
                false
        ));

        // Cameras
        products.add(new Product(
                "prod_13",
                "Alpha 7 IV Mirrorless Camera",
                "Sony",
                "Cameras",
                2398.99,
                2499.99,
                4,
                4.9f,
                "Full-frame 33MP Exmor R CMOS sensor, 4K 60p video recording, and real-time Eye AF for humans, animals, and birds.",
                "Sensor: 33MP Full-Frame Exmor R\nVideo: 4K 60p 10-bit 4:2:2\nAutofocus: 759 Phase-detection points",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500",
                8,
                true
        ));

        products.add(new Product(
                "prod_19",
                "Canon EOS R6 Mark II",
                "Canon",
                "Cameras",
                2499.99,
                2799.99,
                11,
                4.9f,
                "High-speed 40 fps electronic shutter, 24.2MP full-frame CMOS sensor, 6K RAW external recording, and Dual Pixel CMOS AF II.",
                "Sensor: 24.2MP Full-Frame CMOS\nVideo: 4K 60p Oversampled from 6K\nIn-Body IS: Up to 8 stops",
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=500",
                6,
                false
        ));

        // Gaming
        products.add(new Product(
                "prod_14",
                "PlayStation 5 Slim Console",
                "Sony",
                "Gaming",
                449.99,
                499.99,
                10,
                4.9f,
                "Unleash new gaming possibilities with custom ultra-high speed 1TB SSD, haptic feedback, and 3D Audio.",
                "Storage: 1TB Custom SSD\nOutput: 4K 120Hz / 8K Support\nIncludes: DualSense Wireless Controller",
                "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=500",
                22,
                true
        ));

        products.add(new Product(
                "prod_20",
                "Nintendo Switch OLED Model",
                "Nintendo",
                "Gaming",
                349.99,
                379.99,
                8,
                4.8f,
                "Vibrant 7-inch OLED screen, wide adjustable stand, wired LAN port dock, 64GB internal storage, and enhanced audio.",
                "Screen: 7.0\" OLED Multi-touch\nStorage: 64GB Internal\nModes: TV, Tabletop, Handheld",
                "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=500",
                35,
                false
        ));

        // Monitors & Drones
        products.add(new Product(
                "prod_21",
                "DJI Mini 4 Pro Drone",
                "DJI",
                "Monitors & Drones",
                759.99,
                859.99,
                12,
                4.9f,
                "Miniature flagship drone weighing under 249g with 4K/60fps HDR video, Omnidirectional Obstacle Sensing, and 20km FHD video transmission.",
                "Weight: <249g\nCamera: 1/1.3\" CMOS 48MP\nFlight Time: Up to 34 mins",
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=500",
                14,
                true
        ));

        products.add(new Product(
                "prod_22",
                "LG UltraGear 32\" OLED 240Hz",
                "LG",
                "Monitors & Drones",
                1199.99,
                1399.99,
                14,
                4.9f,
                "World's first Dual-Mode gaming monitor featuring 4K 240Hz / FHD 480Hz OLED screen display with 0.03ms response time.",
                "Display: 31.5\" UHD 4K OLED\nRefresh Rate: Dual-Mode 240Hz/480Hz\nResponse: 0.03ms GtG",
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500",
                9,
                true
        ));

        // Smart Home
        products.add(new Product(
                "prod_23",
                "Sonos Era 300 Smart Speaker",
                "Sonos",
                "Smart Home",
                449.99,
                499.99,
                10,
                4.7f,
                "Next-level spatial audio speaker with six optimal drivers positioned all around the front, sides, and top.",
                "Features: Dolby Atmos Spatial Audio, WiFi 6, Bluetooth 5.0, Apple AirPlay 2",
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500",
                18,
                false
        ));

        // Accessories
        products.add(new Product(
                "prod_15",
                "MX Master 3S Wireless Mouse",
                "Logitech",
                "Accessories",
                99.99,
                119.99,
                17,
                4.8f,
                "Ergonomic wireless performance mouse with Quiet Clicks, 8K DPI tracking on any surface, and MagSpeed scrolling wheel.",
                "Sensor: 8000 DPI Darkfield\nConnectivity: Bluetooth + Logi Bolt USB Receiver\nBattery: Up to 70 Days",
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500",
                50,
                false
        ));

        products.add(new Product(
                "prod_24",
                "Anker Prime 20,000mAh Power Bank",
                "Anker",
                "Accessories",
                129.99,
                149.99,
                13,
                4.9f,
                "200W total output portable charger with smart digital display, ultra-fast charging, and 100W rapid recharge capability.",
                "Capacity: 20,000mAh\nMax Output: 200W (Multi-port)\nPorts: 2x USB-C + 1x USB-A",
                "https://images.unsplash.com/photo-1609592424083-d95856407005?w=500",
                45,
                false
        ));

        return products;
    }
}
