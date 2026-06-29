import axios from "axios";
import { store } from "../../redux/store";
import { logout, setCredentials } from "../../redux/authSlice";

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  category: string;
  categoryId?: string;
  rating: number;
  reviewsCount: number;
  isVeg: boolean;
  isBestSeller?: boolean;
  available?: boolean;
  preparationTime?: number;
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
  reviews: any[];
  menu: MenuItem[];
  description?: string;
  phone?: string;
  email?: string;
}

export interface Coupon {
  id?: string;
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
  status: "pending" | "confirmed" | "preparing" | "ready_for_pickup" | "assigned" | "picked_up" | "delivered" | "cancelled";
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

// Configure base axios instance (pointing to Spring Boot API)
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
    if (config.data instanceof FormData && config.headers) {
      delete (config.headers as any)["Content-Type"];
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor with token refresh
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem("qb_refresh_token");
      if (!refreshToken) {
        isRefreshing = false;
        store.dispatch(logout());
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(
          (import.meta.env.VITE_API_URL || "http://localhost:8080/api") + "/v1/auth/refresh",
          { refreshToken }
        );
        const { accessToken, refreshToken: newRefreshToken, email, role, name } = response.data.data;

        localStorage.setItem("qb_auth_token", accessToken);
        localStorage.setItem("qb_refresh_token", newRefreshToken);
        
        store.dispatch(setCredentials({ user: { email, role, name, walletBalance: 0 }, token: accessToken }));

        processQueue(null, accessToken);
        isRefreshing = false;

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;
        store.dispatch(logout());
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

// Helpers
const mapRestaurant = (res: any, menuItems: any[] = []): Restaurant => {
  return {
    id: String(res.id),
    name: res.name,
    cuisine: res.cuisineType ? res.cuisineType.split(",").map((c: any) => c.trim()) : [],
    rating: res.avgRating || 0,
    ratingCount: res.totalReviews ? `${res.totalReviews} ratings` : "0 ratings",
    deliveryTime: res.estimatedDeliveryTime ? `${res.estimatedDeliveryTime} min` : "30 min",
    distance: "2.5 km",
    costForTwo: res.minOrderAmount ? Number(res.minOrderAmount) : 300,
    offerText: "Flat 10% Off on Wallet",
    imageUrl: res.profileImageUrl || "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
    bannerUrl: res.coverImageUrl || "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
    address: res.address ? `${res.address.addressLine1 || ""}, ${res.address.city || ""}` : "Main Street, City",
    isVeg: false,
    isOpen: res.active,
    features: ["Delivery", "Takeaway"],
    reviews: [],
    menu: menuItems.map(mapMenuItem),
    description: res.description || "",
    phone: res.phone || "",
    email: res.email || ""
  };
};

const mapMenuItem = (item: any): MenuItem => {
  const customizationOptions: any[] = [];
  if (item.variants && item.variants.length > 0) {
    customizationOptions.push({
      title: "Choose Size",
      type: "single",
      required: true,
      choices: item.variants.map((v: any) => ({ name: v.name, price: Number(v.price) })),
    });
  }
  if (item.addOns && item.addOns.length > 0) {
    customizationOptions.push({
      title: "Choose Extras",
      type: "multiple",
      required: false,
      choices: item.addOns.map((a: any) => ({ name: a.name, price: Number(a.price) })),
    });
  }

  return {
    id: String(item.id),
    name: item.name,
    description: item.description || "",
    price: Number(item.price),
    image: item.images && item.images.length > 0
      ? (item.images.find((img: any) => img.primary)?.imageUrl || item.images[0].imageUrl)
      : "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
    category: String(item.categoryId || "General"),
    categoryId: item.categoryId ? String(item.categoryId) : undefined,
    rating: 4.8,
    reviewsCount: 120,
    isVeg: item.foodType === "VEG",
    isBestSeller: item.bestseller,
    available: item.available,
    preparationTime: item.preparationTime,
    customizable: customizationOptions.length > 0,
    customizationOptions: customizationOptions.length > 0 ? customizationOptions : undefined,
  };
};

const mapOrder = (o: any): Order => {
  return {
    id: String(o.id || o.orderNumber),
    restaurantId: String(o.restaurantId),
    restaurantName: o.restaurantName,
    restaurantImage: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
    items: o.items ? o.items.map((i: any) => ({
      id: String(i.foodItemId),
      name: i.foodItemName,
      price: Number(i.unitPrice),
      quantity: i.quantity,
      customizations: i.specialInstructions ? [i.specialInstructions] : []
    })) : [],
    subtotal: Number(o.subtotal),
    tax: Number(o.taxAmount),
    deliveryFee: Number(o.deliveryFee),
    discount: Number(o.discount),
    total: Number(o.totalAmount),
    status: mapOrderStatus(o.status),
    date: o.placedAt ? o.placedAt.replace("T", " ").substring(0, 16) : new Date().toISOString(),
    address: o.specialInstructions || "Main Street, City",
    deliveryPartner: o.deliveryPartnerId ? {
      name: o.deliveryPartnerName || "Delivery Partner",
      phone: o.deliveryPartnerPhone || "+91 99999 88888",
      avatar: "https://lh3.googleusercontent.com/aida/AP1WRLsumPJaSJ8xK5IVqkffZwaWA0Ieq4mdKaVAex_S53Ftr4GYj0eMQIqlUvP-Zy07oK3DteKAEgJ48yl4RRt_YSo66lVZ-bKpibVWuRdeZuwcMt2eoD8Ny8leI0srwUAG_izRU50YXwnlcWBNdlQ92oAdyPKvKRiSEyPYhC5VvZ0Nn1Hn-HYJ1rQAfR9si3uKCBZivS2_OsYNq3WwVD-eAHleKn5WFzQFJGoAHRta2cfiayX-UlDE-NxF9n0",
      vehicleNumber: "DL3C-AA-1234",
      rating: 4.9
    } : undefined
  };
};

const mapOrderStatus = (status: string): Order["status"] => {
  const s = status.toLowerCase();
  if (s === "created" || s === "pending") return "pending";
  if (s === "confirmed") return "confirmed";
  if (s === "preparing") return "preparing";
  if (s === "ready_for_pickup") return "ready_for_pickup";
  if (s === "assigned") return "assigned";
  if (s === "picked_up" || s === "out_for_delivery") return "picked_up";
  if (s === "delivered") return "delivered";
  if (s === "cancelled") return "cancelled";
  return "pending";
};

const mapCartResponseToState = (res: any): any => {
  return {
    items: res.items ? res.items.map((i: any) => ({
      id: String(i.id),
      menuItemId: String(i.foodItemId),
      name: i.foodItemName,
      price: Number(i.unitPrice),
      quantity: i.quantity,
      image: i.foodItemImageUrl || "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
      isVeg: false,
      customizations: i.specialInstructions ? i.specialInstructions.split(",") : []
    })) : [],
    restaurantId: res.restaurantId ? String(res.restaurantId) : null,
    restaurantName: res.restaurantName || null,
    restaurantImage: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
    appliedCoupon: res.appliedCouponCode ? {
      code: res.appliedCouponCode,
      discount: Number(res.discount || 0),
      type: "flat",
      minOrder: 0,
      description: "Applied Coupon"
    } : null
  };
};

// Real API layer mapping standard requests to Spring Boot
export const apiService = {
  // Authentication API
  login: async (email: string, password?: string) => {
    const res = await api.post("/v1/auth/login", { email, password: password || "password" });
    const { accessToken, refreshToken, role: returnedRole, name } = res.data.data;
    localStorage.setItem("qb_auth_token", accessToken);
    localStorage.setItem("qb_refresh_token", refreshToken);
    
    const user = {
      email,
      name,
      role: returnedRole,
      walletBalance: 0,
    };
    
    if (returnedRole === "CUSTOMER") {
      try {
        const walletRes = await api.get("/v1/wallets", {
          headers: { Authorization: `Bearer ${accessToken}` }
        });
        user.walletBalance = Number(walletRes.data.data.balance);
      } catch (e) {
        console.error("Failed to fetch wallet balance", e);
      }
    }
    
    localStorage.setItem("qb_user", JSON.stringify(user));
    return { user, token: accessToken };
  },

  register: async (registerData: any) => {
    const res = await api.post("/v1/auth/register", registerData);
    return res.data.data;
  },

  logout: async () => {
    try {
      await api.post("/v1/auth/logout");
    } catch (e) {
      console.error("Logout request failed on backend", e);
    }
    localStorage.removeItem("qb_auth_token");
    localStorage.removeItem("qb_refresh_token");
    localStorage.removeItem("qb_user");
    return true;
  },

  // Restaurant API
  getRestaurants: async (): Promise<Restaurant[]> => {
    const res = await api.get("/v1/restaurants?size=100");
    const list = res.data.data.content;
    return list.map((r: any) => mapRestaurant(r));
  },

  getRestaurantById: async (id: string): Promise<Restaurant | undefined> => {
    const restRes = await api.get(`/v1/restaurants/${id}`);
    const menuRes = await api.get(`/v1/restaurants/${id}/menu/items?size=100`);
    return mapRestaurant(restRes.data.data, menuRes.data.data.content);
  },

  getMyRestaurant: async (): Promise<Restaurant | undefined> => {
    const res = await api.get("/v1/restaurants/my-restaurant");
    const restData = res.data.data;
    const menuRes = await api.get(`/v1/restaurants/${restData.id}/menu/items?size=100`);
    return mapRestaurant(restData, menuRes.data.data.content);
  },

  getCategories: async (restaurantId: string): Promise<any[]> => {
    const res = await api.get(`/v1/restaurants/${restaurantId}/menu/categories`);
    return res.data.data;
  },

  createCategory: async (restaurantId: string, name: string): Promise<any> => {
    const res = await api.post(`/v1/restaurants/${restaurantId}/menu/categories`, { name, sortOrder: 1, active: true });
    return res.data.data;
  },

  updateCategory: async (restaurantId: string, categoryId: string, categoryData: any): Promise<any> => {
    const res = await api.put(`/v1/restaurants/${restaurantId}/menu/categories/${categoryId}`, categoryData);
    return res.data.data;
  },

  deleteCategory: async (restaurantId: string, categoryId: string): Promise<void> => {
    await api.delete(`/v1/restaurants/${restaurantId}/menu/categories/${categoryId}`);
  },

  getMenuPage: async (
    restaurantId: string,
    params: { search?: string; page?: number; size?: number; categoryId?: string }
  ): Promise<{ content: MenuItem[]; totalPages: number; totalElements: number; number: number }> => {
    const res = await api.get(`/v1/restaurants/${restaurantId}/menu/items`, {
      params: {
        search: params.search || undefined,
        page: params.page ?? 0,
        size: params.size ?? 10,
        categoryId: params.categoryId || undefined
      }
    });
    const pageData = res.data.data;
    return {
      content: (pageData.content || []).map(mapMenuItem),
      totalPages: pageData.totalPages || 1,
      totalElements: pageData.totalElements || 0,
      number: pageData.number || 0
    };
  },

  getMenuItem: async (restaurantId: string, itemId: string): Promise<any> => {
    const res = await api.get(`/v1/restaurants/${restaurantId}/menu/items/${itemId}`);
    return res.data.data;
  },

  createMenuItem: async (restaurantId: string, itemData: any): Promise<MenuItem> => {
    const res = await api.post(`/v1/restaurants/${restaurantId}/menu/items`, itemData);
    return mapMenuItem(res.data.data);
  },

  updateMenuItem: async (restaurantId: string, itemId: string, itemData: any): Promise<MenuItem> => {
    const res = await api.put(`/v1/restaurants/${restaurantId}/menu/items/${itemId}`, itemData);
    return mapMenuItem(res.data.data);
  },

  deleteMenuItem: async (restaurantId: string, itemId: string): Promise<void> => {
    await api.delete(`/v1/restaurants/${restaurantId}/menu/items/${itemId}`);
  },

  updateMyRestaurant: async (restaurantData: any): Promise<any> => {
    const res = await api.put("/v1/restaurants/my-restaurant", restaurantData);
    return res.data.data;
  },

  getMerchantRevenue: async (): Promise<any> => {
    const res = await api.get("/v1/restaurants/my-restaurant/revenue");
    return res.data.data;
  },

  getMerchantAnalytics: async (): Promise<any> => {
    const res = await api.get("/v1/restaurants/my-restaurant/analytics");
    return res.data.data;
  },

  uploadImage: async (file: File): Promise<any> => {
    const formData = new FormData();
    formData.append("file", file);
    const res = await api.post("/v1/merchant/uploads/images", formData);
    return res.data.data;
  },

  // Order API
  getOrders: async (): Promise<Order[]> => {
    const userStr = localStorage.getItem("qb_user");
    if (!userStr) return [];
    const user = JSON.parse(userStr);
    
    if (user.role === "CUSTOMER") {
      const response = await api.get("/v1/orders/history?size=100");
      return response.data.data.content.map(mapOrder);
    } else if (user.role === "RESTAURANT") {
      const response = await api.get("/v1/orders/restaurant?size=100");
      return response.data.data.content.map(mapOrder);
    } else if (user.role === "DELIVERY") {
      const response = await api.get("/v1/delivery/history?size=100");
      return response.data.data.content.map(mapOrder);
    } else if (user.role === "ADMIN") {
      const response = await api.get("/v1/admin/orders?size=100");
      return response.data.data.content.map(mapOrder);
    }
    return [];
  },

  getOrderById: async (id: string): Promise<Order | undefined> => {
    const res = await api.get(`/v1/orders/${id}`);
    return mapOrder(res.data.data);
  },

  createOrder: async (orderData: Omit<Order, "id" | "status" | "date" | "deliveryPartner">): Promise<Order> => {
    const addrRes = await api.get("/v1/customers/addresses");
    const addresses = addrRes.data.data;
    const defaultAddr = addresses.find((a: any) => a.isDefault) || addresses[0];
    
    if (!defaultAddr) {
      throw new Error("Please add a delivery address first.");
    }
    
    const placeRequest = {
      deliveryAddressId: defaultAddr.id,
      paymentMethod: orderData.total > 0 ? "WALLET" : "COD",
      specialInstructions: orderData.address
    };
    
    const res = await api.post("/v1/orders", placeRequest);
    return mapOrder(res.data.data);
  },

  updateOrderStatus: async (orderId: string, status: string, cancellationReason?: string) => {
    let backendStatus = status.toUpperCase();
    const res = await api.patch(`/v1/orders/${orderId}/status`, {
      status: backendStatus,
      cancellationReason
    });
    return mapOrder(res.data.data);
  },

  getMerchantOrderQueue: async (): Promise<Order[]> => {
    const response = await api.get("/v1/orders/restaurant/queue?size=100");
    return response.data.data.content.map(mapOrder);
  },

  getPendingDeliveries: async (): Promise<Order[]> => {
    const res = await api.get("/v1/delivery/pending?size=100");
    return res.data.data.content.map(mapOrder);
  },

  acceptDelivery: async (orderId: string): Promise<Order> => {
    const res = await api.post(`/v1/delivery/orders/${orderId}/accept`);
    return mapOrder(res.data.data);
  },

  getRiderProfile: async () => {
    const res = await api.get("/v1/delivery/profile");
    return res.data.data;
  },

  getRiderEarnings: async () => {
    const res = await api.get("/v1/delivery/earnings");
    return res.data.data;
  },

  getPendingOnboarding: async (): Promise<any[]> => {
    const res = await api.get("/v1/admin/restaurants/pending?size=100");
    return res.data.data.content;
  },

  verifyRestaurant: async (restaurantId: string | number, approve: boolean): Promise<any> => {
    const status = approve ? "APPROVED" : "REJECTED";
    const res = await api.patch(`/v1/admin/restaurants/${restaurantId}/verify?status=${status}`);
    return res.data.data;
  },

  getComplaints: async (): Promise<any[]> => {
    const res = await api.get("/v1/admin/complaints?size=100");
    return res.data.data.content;
  },

  resolveComplaint: async (complaintId: string, notes: string): Promise<any> => {
    const res = await api.patch(`/v1/admin/complaints/${complaintId}/resolve`, { resolutionDetails: notes });
    return res.data.data;
  },

  getAdminUsers: async (params: { search?: string; role?: string; status?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params.search) query.append("search", params.search);
    if (params.role) query.append("role", params.role);
    if (params.status) query.append("status", params.status);
    if (params.page !== undefined) query.append("page", String(params.page));
    if (params.size !== undefined) query.append("size", String(params.size));
    const res = await api.get(`/v1/admin/users?${query.toString()}`);
    return res.data.data;
  },

  updateAdminUserStatus: async (userId: string | number, status: string) => {
    const res = await api.put(`/v1/admin/users/${userId}/status?status=${status}`);
    return res.data.data;
  },

  updateAdminUserRole: async (userId: string | number, role: string) => {
    const res = await api.put(`/v1/admin/users/${userId}/role?role=${role}`);
    return res.data.data;
  },

  deleteAdminUser: async (userId: string | number) => {
    const res = await api.delete(`/v1/admin/users/${userId}`);
    return res.data.data;
  },

  getAdminRestaurants: async (params: { search?: string; status?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params.search) query.append("search", params.search);
    if (params.status) query.append("status", params.status);
    if (params.page !== undefined) query.append("page", String(params.page));
    if (params.size !== undefined) query.append("size", String(params.size));
    const res = await api.get(`/v1/admin/restaurants?${query.toString()}`);
    return res.data.data;
  },

  updateAdminRestaurantProfile: async (restaurantId: string | number, payload: any) => {
    const res = await api.put(`/v1/admin/restaurants/${restaurantId}`, payload);
    return res.data.data;
  },

  deleteAdminRestaurant: async (restaurantId: string | number) => {
    const res = await api.delete(`/v1/admin/restaurants/${restaurantId}`);
    return res.data.data;
  },

  getAdminOrders: async (params: { search?: string; status?: string; startDate?: string; endDate?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params.search) query.append("search", params.search);
    if (params.status) query.append("status", params.status);
    if (params.startDate) query.append("startDate", params.startDate);
    if (params.endDate) query.append("endDate", params.endDate);
    if (params.page !== undefined) query.append("page", String(params.page));
    if (params.size !== undefined) query.append("size", String(params.size));
    const res = await api.get(`/v1/admin/orders?${query.toString()}`);
    return {
      content: res.data.data.content.map(mapOrder),
      totalPages: res.data.data.totalPages,
      totalElements: res.data.data.totalElements,
      number: res.data.data.number
    };
  },

  // Coupon API
  getCoupons: async (): Promise<Coupon[]> => {
    const userStr = localStorage.getItem("qb_user");
    let restId: string | undefined;
    if (userStr) {
      const user = JSON.parse(userStr);
      if (user.role === "RESTAURANT") {
        try {
          const rest = await api.get("/v1/restaurants/my-restaurant");
          restId = rest.data.data.id;
        } catch (e) {}
      }
    }
    
    const url = restId ? `/v1/coupons?restaurantId=${restId}&size=100` : "/v1/coupons?size=100";
    const res = await api.get(url);
    return res.data.data.content.map((c: any) => ({
      id: String(c.id),
      code: c.code,
      discount: Number(c.value),
      type: c.couponType.toLowerCase() as "percentage" | "flat",
      minOrder: Number(c.minOrderAmount),
      maxDiscount: c.maxDiscount ? Number(c.maxDiscount) : undefined,
      description: c.description
    }));
  },

  createCoupon: async (couponData: any): Promise<Coupon> => {
    const res = await api.post("/v1/coupons", couponData);
    const c = res.data.data;
    return {
      id: String(c.id),
      code: c.code,
      discount: Number(c.value),
      type: c.couponType.toLowerCase() as "percentage" | "flat",
      minOrder: Number(c.minOrderAmount),
      maxDiscount: c.maxDiscount ? Number(c.maxDiscount) : undefined,
      description: c.description
    };
  },

  updateCoupon: async (couponId: string, couponData: any): Promise<Coupon> => {
    const res = await api.put(`/v1/coupons/${couponId}`, couponData);
    const c = res.data.data;
    return {
      id: String(c.id),
      code: c.code,
      discount: Number(c.value),
      type: c.couponType.toLowerCase() as "percentage" | "flat",
      minOrder: Number(c.minOrderAmount),
      maxDiscount: c.maxDiscount ? Number(c.maxDiscount) : undefined,
      description: c.description
    };
  },

  deleteCoupon: async (couponId: string): Promise<void> => {
    await api.delete(`/v1/coupons/${couponId}`);
  },

  // Wallet API
  getWalletBalance: async (): Promise<number> => {
    const res = await api.get("/v1/wallets");
    return Number(res.data.data.balance);
  },

  addWalletFunds: async (amount: number): Promise<number> => {
    const res = await api.post("/v1/wallets/add-money", { amount });
    return Number(res.data.data.balance);
  },

  getTransactions: async (): Promise<WalletTransaction[]> => {
    const res = await api.get("/v1/wallets/transactions?size=100");
    return res.data.data.content.map((tx: any) => ({
      id: String(tx.id),
      type: tx.transactionType.toLowerCase() as "credit" | "debit",
      amount: Number(tx.amount),
      description: tx.description,
      date: tx.createdAt ? tx.createdAt.replace("T", " ").substring(0, 16) : new Date().toISOString()
    }));
  },

  // Cart API
  getCart: async () => {
    const res = await api.get("/v1/carts");
    return mapCartResponseToState(res.data.data);
  },
  
  addCartItem: async (foodItemId: string, quantity: number, specialInstructions?: string) => {
    const res = await api.post("/v1/carts/items", {
      foodItemId: Number(foodItemId),
      quantity,
      specialInstructions: specialInstructions || ""
    });
    return mapCartResponseToState(res.data.data);
  },
  
  updateCartItem: async (itemId: string, quantity: number) => {
    const res = await api.put(`/v1/carts/items/${itemId}`, { quantity });
    return mapCartResponseToState(res.data.data);
  },
  
  removeCartItem: async (itemId: string) => {
    const res = await api.delete(`/v1/carts/items/${itemId}`);
    return mapCartResponseToState(res.data.data);
  },
  
  clearCart: async () => {
    const res = await api.delete("/v1/carts/clear");
    return mapCartResponseToState(res.data.data);
  },
  
  applyCoupon: async (couponCode: string) => {
    const res = await api.post("/v1/carts/coupon", { couponCode });
    return mapCartResponseToState(res.data.data);
  },
  
  removeCoupon: async () => {
    const res = await api.delete("/v1/carts/coupon");
    return mapCartResponseToState(res.data.data);
  }
};
