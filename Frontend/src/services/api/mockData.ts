// Centralized Mock Database for QuickBite

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  category: string;
  rating: number;
  reviewsCount: number;
  isVeg: boolean;
  isBestSeller?: boolean;
  customizable?: boolean;
  customizationOptions?: {
    title: string;
    type: "single" | "multiple";
    required: boolean;
    choices: { name: string; price: number }[];
  }[];
}

export interface Restaurant {
  id: string;
  name: string;
  cuisine: string[];
  rating: number;
  ratingCount: string;
  deliveryTime: string;
  distance: string;
  costForTwo: number;
  offerText: string;
  imageUrl: string;
  bannerUrl: string;
  address: string;
  isVeg: boolean;
  isOpen: boolean;
  features: string[];
  reviews: {
    id: string;
    userName: string;
    rating: number;
    date: string;
    comment: string;
  }[];
  menu: MenuItem[];
}

export interface Coupon {
  code: string;
  discount: number;
  type: "percentage" | "flat";
  minOrder: number;
  maxDiscount?: number;
  description: string;
}

export interface WalletTransaction {
  id: string;
  type: "credit" | "debit";
  amount: number;
  description: string;
  date: string;
}

export interface Order {
  id: string;
  restaurantId: string;
  restaurantName: string;
  restaurantImage: string;
  items: {
    id: string;
    name: string;
    price: number;
    quantity: number;
    customizations?: string[];
  }[];
  subtotal: number;
  tax: number;
  deliveryFee: number;
  discount: number;
  total: number;
  status: "pending" | "preparing" | "picked_up" | "delivered" | "cancelled";
  date: string;
  deliveryPartner?: {
    name: string;
    phone: string;
    avatar: string;
    vehicleNumber: string;
    rating: number;
  };
  address: string;
}

export interface OnboardingRequest {
  id: string;
  restaurantName: string;
  ownerName: string;
  email: string;
  phone: string;
  cuisine: string[];
  address: string;
  submittedAt: string;
  status: "pending" | "approved" | "rejected";
  licenseNumber: string;
}

export interface SupportTicket {
  id: string;
  orderId?: string;
  userName: string;
  userType: "customer" | "merchant" | "delivery";
  subject: string;
  message: string;
  status: "open" | "resolved";
  createdAt: string;
  replies: {
    sender: "user" | "admin";
    message: string;
    timestamp: string;
  }[];
}

// Initial Mock Database State
export const mockRestaurants: Restaurant[] = [
  {
    id: "a8bd6caddd574bae9da4e934e5887ede", // The Burger House ID from Stitch
    name: "The Burger House",
    cuisine: ["Burgers", "Fast Food", "Sides", "Beverages"],
    rating: 4.6,
    ratingCount: "5K+ ratings",
    deliveryTime: "25-30 mins",
    distance: "1.8 km",
    costForTwo: 350,
    offerText: "50% OFF up to ₹100 | Use WELCOME50",
    imageUrl: "https://lh3.googleusercontent.com/aida/AP1WRLtBXG2_D3PIDoa1kELRo8PppwsUjQ-Ee16sFF7tyjO6OWZhxCxcRTF2OUGi9t6EEu7oZRxNKYWl_xVLFV9_7Py14NZegEyXMoHE1s1AA0wZBkZosX67TZbMwsH-FNuDeeW7Ho6A8eAwU8CscWHpBHZ8jhBJvL0DwzMVJUcmlSLQcUSrC_eBjt9iz0zDCYm1hz83hC34fg1ETWo_40LoRYWZFyAQ4kJvfSxLjES0P4tWpFuCzi_s2-gTAVKi",
    bannerUrl: "https://lh3.googleusercontent.com/aida/AP1WRLs7nMY1aKIPJV_Nm1KGvFxjve-UoGgvhhqzHtSo2R4r-mjBx-SnaoJFFqwnDq6gN7-BCv28p6tP5hAjBkpPjMHI5jxW7GQ0FGtSxiNX9ZIpznKuBkgmPDrQi7EcVe439IHUtdjEjga3AAk2eaTeJ0HaZtabgwRImQhK8YY9wlb5YjxtwVTU8bvHGthSpVpZHWg4YuwntRjvCo0dvQ6x4-Ba46DAHRMikUMZOM9nQ8V7WKWlb_vEw3w0ig",
    address: "Plot 42, Sector 18, Commercial Hub, Noida",
    isVeg: false,
    isOpen: true,
    features: ["Free Delivery", "Contactless Delivery", "Best Seller"],
    reviews: [
      { id: "rev-1", userName: "Aarav Sharma", rating: 5, date: "2 days ago", comment: "The Gourmet Cheese Burger is to die for! Incredibly juicy patties and soft brioche buns." },
      { id: "rev-2", userName: "Priya Patel", rating: 4, date: "1 week ago", comment: "Good taste and super fast delivery. Onion rings were a bit soggy though." },
      { id: "rev-3", userName: "Rahul Verma", rating: 5, date: "2 weeks ago", comment: "Excellent packaging! They separated the sauce so it didn't get soggy. Recommended." }
    ],
    menu: [
      {
        id: "burger-1",
        name: "Classic Cheese Burger",
        description: "Juicy flame-grilled chicken patty topped with melted cheddar, crisp lettuce, tomato, pickles, and house special burger sauce in toasted brioche bun.",
        price: 189,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Burgers",
        rating: 4.5,
        reviewsCount: 120,
        isVeg: false,
        isBestSeller: true,
        customizable: true,
        customizationOptions: [
          {
            title: "Choose Size",
            type: "single",
            required: true,
            choices: [
              { name: "Single Patty", price: 0 },
              { name: "Double Patty (Heavy)", price: 69 },
              { name: "Triple Patty (Super Heavy)", price: 119 }
            ]
          },
          {
            title: "Add Extras",
            type: "multiple",
            required: false,
            choices: [
              { name: "Extra Cheddar Slice", price: 20 },
              { name: "Crispy Bacon Strips", price: 40 },
              { name: "Fried Egg", price: 15 },
              { name: "Caramelized Onions", price: 10 }
            ]
          }
        ]
      },
      {
        id: "burger-2",
        name: "Crispy Veggie Crunch Burger",
        description: "Golden crispy vegetable patty with melted cheese, spicy mayo, fresh lettuce, and tomatoes.",
        price: 149,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Burgers",
        rating: 4.3,
        reviewsCount: 88,
        isVeg: true,
        customizable: true,
        customizationOptions: [
          {
            title: "Add Extras",
            type: "multiple",
            required: false,
            choices: [
              { name: "Extra Cheese", price: 20 },
              { name: "Jalapenos", price: 15 },
              { name: "Sautéed Mushrooms", price: 30 }
            ]
          }
        ]
      },
      {
        id: "burger-3",
        name: "Peri-Peri Spicy Chicken Burger",
        description: "Flame-grilled spicy peri-peri breast fillet with fiery sauce, fresh coleslaw, jalapenos, and red onions.",
        price: 219,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Burgers",
        rating: 4.7,
        reviewsCount: 210,
        isVeg: false,
        isBestSeller: true
      },
      {
        id: "side-1",
        name: "Salted Golden Fries",
        description: "Classic potato fries, fried golden brown and lightly salted. Crispy outside, fluffy inside.",
        price: 99,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Sides",
        rating: 4.4,
        reviewsCount: 340,
        isVeg: true
      },
      {
        id: "side-2",
        name: "Peri-Peri Dusted Fries",
        description: "Vibrant golden fries dusted with hot and tangy Peri-Peri seasoning.",
        price: 119,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Sides",
        rating: 4.6,
        reviewsCount: 420,
        isVeg: true,
        isBestSeller: true
      },
      {
        id: "bev-1",
        name: "Chilled Coca-Cola (330ml)",
        description: "Perfect carbonated refreshment to go with your burger.",
        price: 50,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Beverages",
        rating: 4.8,
        reviewsCount: 1200,
        isVeg: true
      }
    ]
  },
  {
    id: "pizza-palazzo-id",
    name: "Pizza Palazzo",
    cuisine: ["Pizza", "Italian", "Desserts"],
    rating: 4.4,
    ratingCount: "2K+ ratings",
    deliveryTime: "30-35 mins",
    distance: "3.2 km",
    costForTwo: 500,
    offerText: "₹100 OFF on orders above ₹399",
    imageUrl: "https://lh3.googleusercontent.com/aida/AP1WRLtBXG2_D3PIDoa1kELRo8PppwsUjQ-Ee16sFF7tyjO6OWZhxCxcRTF2OUGi9t6EEu7oZRxNKYWl_xVLFV9_7Py14NZegEyXMoHE1s1AA0wZBkZosX67TZbMwsH-FNuDeeW7Ho6A8eAwU8CscWHpBHZ8jhBJvL0DwzMVJUcmlSLQcUSrC_eBjt9iz0zDCYm1hz83hC34fg1ETWo_40LoRYWZFyAQ4kJvfSxLjES0P4tWpFuCzi_s2-gTAVKi",
    bannerUrl: "https://lh3.googleusercontent.com/aida/AP1WRLs7nMY1aKIPJV_Nm1KGvFxjve-UoGgvhhqzHtSo2R4r-mjBx-SnaoJFFqwnDq6gN7-BCv28p6tP5hAjBkpPjMHI5jxW7GQ0FGtSxiNX9ZIpznKuBkgmPDrQi7EcVe439IHUtdjEjga3AAk2eaTeJ0HaZtabgwRImQhK8YY9wlb5YjxtwVTU8bvHGthSpVpZHWg4YuwntRjvCo0dvQ6x4-Ba46DAHRMikUMZOM9nQ8V7WKWlb_vEw3w0ig",
    address: "Block C, Market Road, Sector 62, Noida",
    isVeg: true,
    isOpen: true,
    features: ["Free Delivery"],
    reviews: [],
    menu: [
      {
        id: "pizza-1",
        name: "Double Cheese Margherita Pizza",
        description: "Classic Margherita loaded with extra liquid cheese and stretch mozzarella, baked to crisp.",
        price: 249,
        image: "https://lh3.googleusercontent.com/aida/AP1WRLvyO4re2pTi9YXuZj5HhUbPK8DY3HWyEmHPGc2wuamkHwxfJhNfTwy-9RTkS0XpnqK7Z79Ps0bKCds-yDCHWuZime6ZlEq5OVYpaCwMmgtaIKSKrqUDntVyAzUrel5NYyHeBg16bAK52vBcO0bUgSx3xfRR7U-G5exMCmhedPrwaP1wB7s6WslgjNVVuVcrZV2iFAPnFqtebEow9WzKVlXy-Z9kZdr5aJgdqSaPNpVIki4AFZUVx4DSSfPJ",
        category: "Pizza",
        rating: 4.6,
        reviewsCount: 310,
        isVeg: true,
        isBestSeller: true
      }
    ]
  }
];

export const mockCoupons: Coupon[] = [
  { code: "WELCOME50", discount: 50, type: "percentage", minOrder: 150, maxDiscount: 100, description: "Get 50% discount up to ₹100 on your first order." },
  { code: "QUICKBITE100", discount: 100, type: "flat", minOrder: 499, description: "Get a flat ₹100 discount on orders above ₹499." },
  { code: "BINGE20", discount: 20, type: "percentage", minOrder: 300, maxDiscount: 60, description: "Save 20% on orders above ₹300." }
];

export const mockTransactions: WalletTransaction[] = [
  { id: "tx-001", type: "credit", amount: 1000, description: "Added money from UPI", date: "2026-06-25 14:30" },
  { id: "tx-002", type: "debit", amount: 289, description: "Paid for Order #QB-88219", date: "2026-06-26 12:15" },
  { id: "tx-003", type: "credit", amount: 50, description: "Cashback Reward Received", date: "2026-06-26 12:18" }
];

export const mockOrders: Order[] = [
  {
    id: "QB-88219", // Stitch Order Details ID matches
    restaurantId: "a8bd6caddd574bae9da4e934e5887ede",
    restaurantName: "The Burger House",
    restaurantImage: "https://lh3.googleusercontent.com/aida/AP1WRLtBXG2_D3PIDoa1kELRo8PppwsUjQ-Ee16sFF7tyjO6OWZhxCxcRTF2OUGi9t6EEu7oZRxNKYWl_xVLFV9_7Py14NZegEyXMoHE1s1AA0wZBkZosX67TZbMwsH-FNuDeeW7Ho6A8eAwU8CscWHpBHZ8jhBJvL0DwzMVJUcmlSLQcUSrC_eBjt9iz0zDCYm1hz83hC34fg1ETWo_40LoRYWZFyAQ4kJvfSxLjES0P4tWpFuCzi_s2-gTAVKi",
    items: [
      { id: "burger-1", name: "Classic Cheese Burger", price: 189, quantity: 1, customizations: ["Double Patty (+₹69)", "Extra Cheddar Slice (+₹20)"] },
      { id: "side-2", name: "Peri-Peri Dusted Fries", price: 119, quantity: 1 }
    ],
    subtotal: 278,
    tax: 14,
    deliveryFee: 30,
    discount: 50,
    total: 272,
    status: "delivered",
    date: "2026-06-26 12:15",
    deliveryPartner: {
      name: "Suresh Kumar",
      phone: "+91 98765 43210",
      avatar: "https://lh3.googleusercontent.com/aida/AP1WRLsumPJaSJ8xK5IVqkffZwaWA0Ieq4mdKaVAex_S53Ftr4GYj0eMQIqlUvP-Zy07oK3DteKAEgJ48yl4RRt_YSo66lVZ-bKpibVWuRdeZuwcMt2eoD8Ny8leI0srwUAG_izRU50YXwnlcWBNdlQ92oAdyPKvKRiSEyPYhC5VvZ0Nn1Hn-HYJ1rQAfR9si3uKCBZivS2_OsYNq3WwVD-eAHleKn5WFzQFJGoAHRta2cfiayX-UlDE-NxF9n0",
      vehicleNumber: "UP16-CZ-4321",
      rating: 4.8
    },
    address: "H-15, Sector 44, Noida, UP, 201301"
  }
];

export const mockOnboardingRequests: OnboardingRequest[] = [
  {
    id: "req-001",
    restaurantName: "Spicy Tadka Dhaba",
    ownerName: "Harpreet Singh",
    email: "harpreet@spicytadka.com",
    phone: "+91 91234 56789",
    cuisine: ["North Indian", "Tandoori"],
    address: "Shop 12, Gole Market, Noida Sector 15",
    submittedAt: "2026-06-26 10:00",
    status: "pending",
    licenseNumber: "FSSAI-120938402948"
  },
  {
    id: "req-002",
    restaurantName: "Waffle Wonders",
    ownerName: "Nisha Goel",
    email: "nisha@wafflewonders.com",
    phone: "+91 93456 78901",
    cuisine: ["Desserts", "Bakery"],
    address: "Kiosk 4, DLF Mall of India, Noida Sector 18",
    submittedAt: "2026-06-25 15:45",
    status: "approved",
    licenseNumber: "FSSAI-220194830193"
  }
];

export const mockSupportTickets: SupportTicket[] = [
  {
    id: "ticket-101",
    orderId: "QB-88219",
    userName: "Ayush Malik",
    userType: "customer",
    subject: "Missing item in order",
    message: "I ordered Peri-Peri Dusted Fries but received normal salted fries. Please refund the difference.",
    status: "open",
    createdAt: "2026-06-26 13:00",
    replies: [
      { sender: "user", message: "My order contains wrong items. Please check.", timestamp: "2026-06-26 13:00" }
    ]
  }
];

// Helper database manager (stored in localStorage if running in browser)
export class MockDatabase {
  private static getStored<T>(key: string, defaultVal: T): T {
    const val = localStorage.getItem(key);
    return val ? JSON.parse(val) : defaultVal;
  }

  private static setStored<T>(key: string, val: T): void {
    localStorage.setItem(key, JSON.stringify(val));
  }

  static getRestaurants(): Restaurant[] {
    return this.getStored("qb_restaurants", mockRestaurants);
  }

  static getOrders(): Order[] {
    return this.getStored("qb_orders", mockOrders);
  }

  static getOnboarding(): OnboardingRequest[] {
    return this.getStored("qb_onboarding", mockOnboardingRequests);
  }

  static getTickets(): SupportTicket[] {
    return this.getStored("qb_tickets", mockSupportTickets);
  }

  static saveOrders(orders: Order[]) {
    this.setStored("qb_orders", orders);
  }

  static addOrder(order: Order) {
    const orders = this.getOrders();
    orders.unshift(order);
    this.saveOrders(orders);
  }
}
