import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

export interface User {
  name: string;
  email: string;
  role: "CUSTOMER" | "RESTAURANT" | "DELIVERY" | "ADMIN";
  walletBalance: number;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  token: string | null;
}

const getInitialUser = (): User | null => {
  const userStr = localStorage.getItem("qb_user");
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }
  return null;
};

const initialState: AuthState = {
  user: getInitialUser(),
  isAuthenticated: !!localStorage.getItem("qb_auth_token"),
  token: localStorage.getItem("qb_auth_token"),
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials(
      state,
      action: PayloadAction<{ user: User; token: string }>
    ) {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;
      localStorage.setItem("qb_auth_token", action.payload.token);
      localStorage.setItem("qb_user", JSON.stringify(action.payload.user));
    },
    updateWalletBalance(state, action: PayloadAction<number>) {
      if (state.user) {
        state.user.walletBalance = action.payload;
        localStorage.setItem("qb_user", JSON.stringify(state.user));
      }
    },
    logout(state) {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      localStorage.removeItem("qb_auth_token");
      localStorage.removeItem("qb_user");
    },
  },
});

export const { setCredentials, updateWalletBalance, logout } = authSlice.actions;
export default authSlice.reducer;
