import axios from "axios";
import { MockDatabase, mockCoupons, mockTransactions } from "./mockData";
import type { Restaurant, Order, Coupon, WalletTransaction } from "./mockData";

// Configure base axios instance (pointing to future Spring Boot API)
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("qb_auth_token");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle standard errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Intercept 401s or 403s to clear session if needed
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("qb_auth_token");
      window.location.href = "/auth/login";
    }
    return Promise.reject(error);
  }
);

// Reusable mock delay utility
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms));

// Mock API layer mapping standard requests to mock local storage database
export const apiService = {
  // Authentication Mock
  login: async (email: string, role: string) => {
    await delay(500);
    const token = "mock-jwt-token-xyz-123";
    localStorage.setItem("qb_auth_token", token);
    const user = {
      email,
      name: email.split("@")[0].toUpperCase(),
      role: role.toUpperCase() as "CUSTOMER" | "RESTAURANT" | "DELIVERY" | "ADMIN",
      walletBalance: 762,
    };
    localStorage.setItem("qb_user", JSON.stringify(user));
    return user;
  },

  logout: async () => {
    await delay(200);
    localStorage.removeItem("qb_auth_token");
    localStorage.removeItem("qb_user");
    return true;
  },

  // Restaurant API
  getRestaurants: async (): Promise<Restaurant[]> => {
    await delay(300);
    return MockDatabase.getRestaurants();
  },

  getRestaurantById: async (id: string): Promise<Restaurant | undefined> => {
    await delay(200);
    return MockDatabase.getRestaurants().find((r) => r.id === id);
  },

  // Order API
  getOrders: async (): Promise<Order[]> => {
    await delay(400);
    return MockDatabase.getOrders();
  },

  getOrderById: async (id: string): Promise<Order | undefined> => {
    await delay(200);
    return MockDatabase.getOrders().find((o) => o.id === id);
  },

  createOrder: async (orderData: Omit<Order, "id" | "status" | "date" | "deliveryPartner">): Promise<Order> => {
    await delay(600);
    const newOrder: Order = {
      ...orderData,
      id: `QB-${Math.floor(10000 + Math.random() * 90000)}`,
      status: "preparing",
      date: new Date().toISOString().replace("T", " ").substring(0, 16),
      deliveryPartner: {
        name: "Ramesh Sharma",
        phone: "+91 99999 88888",
        avatar: "https://lh3.googleusercontent.com/aida/AP1WRLsumPJaSJ8xK5IVqkffZwaWA0Ieq4mdKaVAex_S53Ftr4GYj0eMQIqlUvP-Zy07oK3DteKAEgJ48yl4RRt_YSo66lVZ-bKpibVWuRdeZuwcMt2eoD8Ny8leI0srwUAG_izRU50YXwnlcWBNdlQ92oAdyPKvKRiSEyPYhC5VvZ0Nn1Hn-HYJ1rQAfR9si3uKCBZivS2_OsYNq3WwVD-eAHleKn5WFzQFJGoAHRta2cfiayX-UlDE-NxF9n0",
        vehicleNumber: "DL3C-AA-1234",
        rating: 4.9
      }
    };
    MockDatabase.addOrder(newOrder);
    return newOrder;
  },

  // Coupon API
  getCoupons: async (): Promise<Coupon[]> => {
    await delay(200);
    return mockCoupons;
  },

  // Wallet API
  getWalletBalance: async (): Promise<number> => {
    await delay(100);
    const userStr = localStorage.getItem("qb_user");
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.walletBalance || 0;
    }
    return 762;
  },

  addWalletFunds: async (amount: number): Promise<number> => {
    await delay(500);
    const userStr = localStorage.getItem("qb_user");
    if (userStr) {
      const user = JSON.parse(userStr);
      user.walletBalance = (user.walletBalance || 0) + amount;
      localStorage.setItem("qb_user", JSON.stringify(user));
      
      // Save transaction
      const txs = JSON.parse(localStorage.getItem("qb_transactions") || JSON.stringify(mockTransactions));
      txs.unshift({
        id: `tx-${Math.floor(100 + Math.random() * 900)}`,
        type: "credit",
        amount,
        description: "Funds Loaded via Card/UPI",
        date: new Date().toISOString().replace("T", " ").substring(0, 16)
      });
      localStorage.setItem("qb_transactions", JSON.stringify(txs));

      return user.walletBalance;
    }
    return amount;
  },

  getTransactions: async (): Promise<WalletTransaction[]> => {
    await delay(300);
    const txsStr = localStorage.getItem("qb_transactions");
    if (txsStr) {
      return JSON.parse(txsStr);
    }
    localStorage.setItem("qb_transactions", JSON.stringify(mockTransactions));
    return mockTransactions;
  }
};
