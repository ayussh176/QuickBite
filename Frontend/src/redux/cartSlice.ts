import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { MenuItem, Coupon } from "../services/api/mockData";

export interface CartItem {
  id: string; // unique item id including customizations
  menuItemId: string; // original item id
  name: string;
  price: number;
  quantity: number;
  image: string;
  isVeg: boolean;
  customizations?: string[];
}

interface CartState {
  items: CartItem[];
  restaurantId: string | null;
  restaurantName: string | null;
  restaurantImage: string | null;
  appliedCoupon: Coupon | null;
}

const initialState: CartState = {
  items: [],
  restaurantId: null,
  restaurantName: null,
  restaurantImage: null,
  appliedCoupon: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    addToCart(
      state,
      action: PayloadAction<{
        item: MenuItem;
        quantity: number;
        customizations?: string[];
        customizationPrice: number;
        restaurant: { id: string; name: string; image: string };
      }>
    ) {
      const { item, quantity, customizations, customizationPrice, restaurant } = action.payload;

      // Check if adding from a different restaurant
      if (state.restaurantId && state.restaurantId !== restaurant.id) {
        // Clear cart for new restaurant
        state.items = [];
        state.appliedCoupon = null;
      }

      state.restaurantId = restaurant.id;
      state.restaurantName = restaurant.name;
      state.restaurantImage = restaurant.image;

      // Unique key for item + its customizations to handle separate custom stacks in cart
      const customKey = customizations && customizations.length > 0
        ? `${item.id}-${customizations.join(",")}`
        : item.id;

      const existingItem = state.items.find((i) => i.id === customKey);
      const unitPrice = item.price + customizationPrice;

      if (existingItem) {
        existingItem.quantity += quantity;
      } else {
        state.items.push({
          id: customKey,
          menuItemId: item.id,
          name: item.name,
          price: unitPrice,
          quantity,
          image: item.image,
          isVeg: item.isVeg,
          customizations,
        });
      }
    },
    removeFromCart(state, action: PayloadAction<string>) {
      const customKey = action.payload;
      const existingItem = state.items.find((i) => i.id === customKey);
      if (existingItem) {
        if (existingItem.quantity > 1) {
          existingItem.quantity -= 1;
        } else {
          state.items = state.items.filter((i) => i.id !== customKey);
        }
      }

      if (state.items.length === 0) {
        state.restaurantId = null;
        state.restaurantName = null;
        state.restaurantImage = null;
        state.appliedCoupon = null;
      }
    },
    clearItem(state, action: PayloadAction<string>) {
      state.items = state.items.filter((i) => i.id !== action.payload);
      if (state.items.length === 0) {
        state.restaurantId = null;
        state.restaurantName = null;
        state.restaurantImage = null;
        state.appliedCoupon = null;
      }
    },
    applyCoupon(state, action: PayloadAction<Coupon | null>) {
      state.appliedCoupon = action.payload;
    },
    clearCart(state) {
      state.items = [];
      state.restaurantId = null;
      state.restaurantName = null;
      state.restaurantImage = null;
      state.appliedCoupon = null;
    },
    setCart(state, action: PayloadAction<CartState>) {
      state.items = action.payload.items;
      state.restaurantId = action.payload.restaurantId;
      state.restaurantName = action.payload.restaurantName;
      state.restaurantImage = action.payload.restaurantImage;
      state.appliedCoupon = action.payload.appliedCoupon;
    },
  },
});

export const { addToCart, removeFromCart, clearItem, applyCoupon, clearCart, setCart } = cartSlice.actions;
export default cartSlice.reducer;
