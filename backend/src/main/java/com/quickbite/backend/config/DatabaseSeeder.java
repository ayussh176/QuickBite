package com.quickbite.backend.config;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.customer.entity.CustomerAddress;
import com.quickbite.backend.customer.repository.CustomerAddressRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.restaurant.entity.RestaurantAddress;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.delivery.entity.Vehicle;
import com.quickbite.backend.common.enums.*;
import com.quickbite.backend.wallet.entity.Wallet;
import com.quickbite.backend.wallet.repository.WalletRepository;
import com.quickbite.backend.cart.entity.Cart;
import com.quickbite.backend.cart.repository.CartRepository;
import com.quickbite.backend.menu.entity.FoodCategory;
import com.quickbite.backend.menu.repository.FoodCategoryRepository;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.coupon.entity.Coupon;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.payment.entity.UPIConfiguration;
import com.quickbite.backend.payment.repository.UPIConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final WalletRepository walletRepository;
    private final CartRepository cartRepository;
    private final FoodCategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;
    private final UPIConfigurationRepository upiRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0 && restaurantRepository.count() > 0) {
            log.info("Database already seeded. Skipping database seeder.");
            return;
        }

        log.info("Starting database seeding...");

        String encodedPassword = passwordEncoder.encode("password");

        // 1. Seed Admin
        User adminUser = User.builder()
                .email("admin@quickbite.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("9999999999")
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build();
        userRepository.save(adminUser);
        log.info("Admin user created.");

        // 2. Seed Customer
        User customerUser = User.builder()
                .email("customer@quickbite.com")
                .password(encodedPassword)
                .phone("8888888888")
                .role(Role.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build();
        User savedCustUser = userRepository.save(customerUser);

        Customer customer = Customer.builder()
                .user(savedCustUser)
                .firstName("John")
                .lastName("Doe")
                .build();
        Customer savedCust = customerRepository.save(customer);

        CustomerAddress address = CustomerAddress.builder()
                .customer(savedCust)
                .addressType(AddressType.HOME)
                .label("Home")
                .addressLine1("123, Park Avenue")
                .addressLine2("Sector 62")
                .city("Noida")
                .state("Uttar Pradesh")
                .zipCode("201301")
                .latitude(28.6273)
                .longitude(77.3725)
                .isDefault(true)
                .build();
        addressRepository.save(address);

        Wallet custWallet = Wallet.builder()
                .user(savedCustUser)
                .balance(BigDecimal.valueOf(2000.00))
                .build();
        walletRepository.save(custWallet);

        Cart cart = Cart.builder()
                .customer(savedCust)
                .totalAmount(BigDecimal.ZERO)
                .build();
        cartRepository.save(cart);

        log.info("Customer user, address, wallet, and cart created.");

        // 3. Seed Restaurant
        User restaurantUser = User.builder()
                .email("merchant@quickbite.com")
                .password(encodedPassword)
                .phone("7777777777")
                .role(Role.RESTAURANT)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build();
        User savedRestUser = userRepository.save(restaurantUser);

        Restaurant restaurant = Restaurant.builder()
                .user(savedRestUser)
                .name("The Burger House")
                .slug("the-burger-house-tbh123")
                .description("Gourmet burgers and delicious fast food.")
                .cuisineType("Burgers, Fast Food, Beverages")
                .phone("7777777777")
                .email("merchant@quickbite.com")
                .status(RestaurantStatus.APPROVED)
                .fssaiLicense("12345678901234")
                .gstNumber("09AAAAA1111A1Z1")
                .avgRating(BigDecimal.valueOf(4.6))
                .totalReviews(42)
                .openingTime(LocalTime.of(10, 0))
                .closingTime(LocalTime.of(22, 0))
                .minOrderAmount(BigDecimal.valueOf(100.00))
                .deliveryFee(BigDecimal.valueOf(30.00))
                .estimatedDeliveryTime(30)
                .active(true)
                .featured(true)
                .profileImageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd")
                .coverImageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd")
                .build();
        
        RestaurantAddress restAddr = RestaurantAddress.builder()
                .restaurant(restaurant)
                .addressLine1("Plot 42, Sector 18")
                .city("Noida")
                .state("Uttar Pradesh")
                .zipCode("201301")
                .latitude(28.5708)
                .longitude(77.3258)
                .build();
        restaurant.setAddress(restAddr);
        Restaurant savedRest = restaurantRepository.save(restaurant);

        Wallet restWallet = Wallet.builder()
                .user(savedRestUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(restWallet);

        UPIConfiguration upi = UPIConfiguration.builder()
                .restaurant(savedRest)
                .upiId("burgerhouse@upi")
                .providerName("GPay")
                .isDefault(true)
                .isActive(true)
                .build();
        upiRepository.save(upi);

        log.info("Restaurant, address, wallet, and UPI config created.");

        // 4. Seed Delivery Partner
        User deliveryUser = User.builder()
                .email("delivery@quickbite.com")
                .password(encodedPassword)
                .phone("6666666666")
                .role(Role.DELIVERY)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build();
        User savedDelUser = userRepository.save(deliveryUser);

        DeliveryPartner partner = DeliveryPartner.builder()
                .user(savedDelUser)
                .firstName("Ramesh")
                .lastName("Sharma")
                .phone("6666666666")
                .status(DeliveryPartnerStatus.AVAILABLE)
                .verified(true)
                .avgRating(BigDecimal.valueOf(4.9))
                .totalDeliveries(120)
                .profileImageUrl("https://lh3.googleusercontent.com/aida/AP1WRLsumPJaSJ8xK5IVqkffZwaWA0Ieq4mdKaVAex_S53Ftr4GYj0eMQIqlUvP-Zy07oK3DteKAEgJ48yl4RRt_YSo66lVZ-bKpibVWuRdeZuwcMt2eoD8Ny8leI0srwUAG_izRU50YXwnlcWBNdlQ92oAdyPKvKRiSEyPYhC5VvZ0Nn1Hn-HYJ1rQAfR9si3uKCBZivS2_OsYNq3WwVD-eAHleKn5WFzQFJGoAHRta2cfiayX-UlDE-NxF9n0")
                .drivingLicenseNumber("DL-1234567890")
                .aadharNumber("123456789012")
                .build();
        
        Vehicle vehicle = Vehicle.builder()
                .deliveryPartner(partner)
                .registrationNumber("DL-3C-AA-1234")
                .vehicleType(VehicleType.BIKE)
                .model("Hero Splendor")
                .build();
        partner.setVehicle(vehicle);
        DeliveryPartner savedPartner = deliveryPartnerRepository.save(partner);

        Wallet delWallet = Wallet.builder()
                .user(savedDelUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(delWallet);

        log.info("Delivery partner, vehicle, and wallet created.");

        // 5. Seed Menu Categories
        FoodCategory cat1 = FoodCategory.builder()
                .restaurant(savedRest)
                .name("Burgers")
                .sortOrder(1)
                .active(true)
                .build();
        FoodCategory savedCat1 = categoryRepository.save(cat1);

        FoodCategory cat2 = FoodCategory.builder()
                .restaurant(savedRest)
                .name("Sides")
                .sortOrder(2)
                .active(true)
                .build();
        FoodCategory savedCat2 = categoryRepository.save(cat2);

        FoodCategory cat3 = FoodCategory.builder()
                .restaurant(savedRest)
                .name("Beverages")
                .sortOrder(3)
                .active(true)
                .build();
        FoodCategory savedCat3 = categoryRepository.save(cat3);

        log.info("Menu categories created.");

        // 6. Seed Food Items
        FoodItem item1 = FoodItem.builder()
                .restaurant(savedRest)
                .category(savedCat1)
                .name("Classic Cheese Burger")
                .slug("classic-cheese-burger")
                .description("Juicy beef/veg patty layered with melted cheddar, fresh lettuce, tomatoes, and house sauce.")
                .price(BigDecimal.valueOf(149.00))
                .discountedPrice(BigDecimal.valueOf(129.00))
                .foodType(FoodType.VEG)
                .available(true)
                .bestseller(true)
                .preparationTime(15)
                .build();
        FoodItem savedItem1 = foodItemRepository.save(item1);

        Inventory inv1 = Inventory.builder()
                .foodItem(savedItem1)
                .quantity(50)
                .lowStockThreshold(5)
                .build();
        inventoryRepository.save(inv1);

        FoodItem item2 = FoodItem.builder()
                .restaurant(savedRest)
                .category(savedCat1)
                .name("Double Spicy Chicken Burger")
                .slug("double-spicy-chicken-burger")
                .description("Two crispy chicken patties with spicy mayo, jalapenos, and melted Swiss cheese.")
                .price(BigDecimal.valueOf(229.00))
                .foodType(FoodType.NON_VEG)
                .available(true)
                .bestseller(false)
                .preparationTime(20)
                .build();
        FoodItem savedItem2 = foodItemRepository.save(item2);

        Inventory inv2 = Inventory.builder()
                .foodItem(savedItem2)
                .quantity(30)
                .lowStockThreshold(5)
                .build();
        inventoryRepository.save(inv2);

        FoodItem item3 = FoodItem.builder()
                .restaurant(savedRest)
                .category(savedCat2)
                .name("Crispy French Fries")
                .slug("crispy-french-fries")
                .description("Golden, salted potato fries served with tomato ketchup.")
                .price(BigDecimal.valueOf(89.00))
                .foodType(FoodType.VEG)
                .available(true)
                .bestseller(true)
                .preparationTime(10)
                .build();
        FoodItem savedItem3 = foodItemRepository.save(item3);

        Inventory inv3 = Inventory.builder()
                .foodItem(savedItem3)
                .quantity(100)
                .lowStockThreshold(10)
                .build();
        inventoryRepository.save(inv3);

        FoodItem item4 = FoodItem.builder()
                .restaurant(savedRest)
                .category(savedCat3)
                .name("Iced Chocolate Frappe")
                .slug("iced-chocolate-frappe")
                .description("Chilled chocolate coffee blended with milk, whipped cream, and chocolate syrup.")
                .price(BigDecimal.valueOf(119.00))
                .foodType(FoodType.VEG)
                .available(true)
                .bestseller(false)
                .preparationTime(8)
                .build();
        FoodItem savedItem4 = foodItemRepository.save(item4);

        Inventory inv4 = Inventory.builder()
                .foodItem(savedItem4)
                .quantity(40)
                .lowStockThreshold(5)
                .build();
        inventoryRepository.save(inv4);

        log.info("Menu items and inventories created.");

        // 7. Seed Coupons
        Coupon coupon1 = Coupon.builder()
                .code("WELCOME50")
                .couponType(CouponType.PERCENTAGE)
                .value(BigDecimal.valueOf(50.00))
                .minOrderAmount(BigDecimal.valueOf(199.00))
                .maxDiscount(BigDecimal.valueOf(100.00))
                .description("50% off on your first order up to ₹100.")
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusMonths(6))
                .active(true)
                .build();
        couponRepository.save(coupon1);

        Coupon coupon2 = Coupon.builder()
                .code("FLAT100")
                .couponType(CouponType.FLAT)
                .value(BigDecimal.valueOf(100.00))
                .minOrderAmount(BigDecimal.valueOf(499.00))
                .description("Flat ₹100 off on orders above ₹499.")
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusMonths(6))
                .active(true)
                .build();
        couponRepository.save(coupon2);

        log.info("Promo coupons seeded.");
        log.info("Database seeding completed successfully!");
    }
}
