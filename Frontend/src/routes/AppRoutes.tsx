import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../redux/store";
import { CustomerLayout } from "../components/layout/CustomerLayout";
import { MerchantLayout } from "../components/layout/MerchantLayout";
import { AdminLayout } from "../components/layout/AdminLayout";
import { DeliveryLayout } from "../components/layout/DeliveryLayout";

// Import Delivery Pages
import {
  DeliveryDashboard,
  DeliveryOrders,
  DeliveryProfile,
} from "../pages/DeliveryPages";

// Import Auth Pages
import {
  Welcome,
  Login,
  Join,
  ForgotPassword,
  VerifyOTP,
  ResetPassword,
  ResetSuccess,
  VerifyEmail,
} from "../pages/AuthPages";

// Import Customer Pages
import {
  CustomerHome,
  BrowseRestaurants,
  RestaurantDetails,
  AboutRestaurant,
  RestaurantReviews,
  ShoppingCart,
  Checkout,
  SecurePayment,
  OrderTracking,
  OrderConfirmed,
  OrderDelivered,
  CustomerDashboard,
  OrderHistory,
  OrderDetails,
  WalletPayments,
  AccountSettings,
  EditProfile,
  SavedWishlist,
  HelpCenter,
} from "../pages/CustomerPages";

// Import Merchant Pages
import {
  MerchantDashboard,
  MerchantMenu,
  MerchantEditItem,
  MerchantOrders,
  MerchantRevenue,
  MerchantProfile,
  MerchantStaff,
  MerchantOffers,
  MerchantReviews,
} from "../pages/MerchantPages";

// Import Admin Pages
import {
  AdminDashboard,
  AdminOnboarding,
  AdminAnalytics,
  AdminSupport,
} from "../pages/AdminPages";

// Role Protection Wrapper Component
const ProtectedRoute: React.FC<{
  children: React.ReactNode;
  allowedRole: "CUSTOMER" | "RESTAURANT" | "DELIVERY" | "ADMIN";
}> = ({ children, allowedRole }) => {
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);

  if (!isAuthenticated || !user) {
    return <Navigate to="/auth/login" replace />;
  }

  if (user.role !== allowedRole) {
    // Redirect if role doesn't match
    if (user.role === "CUSTOMER") return <Navigate to="/customer/home" replace />;
    if (user.role === "RESTAURANT") return <Navigate to="/merchant/dashboard" replace />;
    if (user.role === "DELIVERY") return <Navigate to="/delivery/dashboard" replace />;
    if (user.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/auth/login" replace />;
  }

  return <>{children}</>;
};

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<Welcome />} />
      <Route path="/auth/login" element={<Login />} />
      <Route path="/auth/register" element={<Join />} />
      <Route path="/auth/forgot-password" element={<ForgotPassword />} />
      <Route path="/auth/verify-otp" element={<VerifyOTP />} />
      <Route path="/auth/reset-password" element={<ResetPassword />} />
      <Route path="/auth/reset-success" element={<ResetSuccess />} />
      <Route path="/auth/verify-email" element={<VerifyEmail />} />

      {/* Customer Protected Pages */}
      <Route
        path="/customer/*"
        element={
          <ProtectedRoute allowedRole="CUSTOMER">
            <CustomerLayout>
              <Routes>
                <Route path="home" element={<CustomerHome />} />
                <Route path="restaurants" element={<BrowseRestaurants />} />
                <Route path="restaurant/:id" element={<RestaurantDetails />} />
                <Route path="restaurant/:id/about" element={<AboutRestaurant />} />
                <Route path="restaurant/:id/reviews" element={<RestaurantReviews />} />
                <Route path="cart" element={<ShoppingCart />} />
                <Route path="checkout" element={<Checkout />} />
                <Route path="payment" element={<SecurePayment />} />
                <Route path="order-tracking" element={<OrderTracking />} />
                <Route path="order-confirmed" element={<OrderConfirmed />} />
                <Route path="order-delivered" element={<OrderDelivered />} />
                <Route path="dashboard" element={<CustomerDashboard />} />
                <Route path="orders" element={<OrderHistory />} />
                <Route path="order/:id" element={<OrderDetails />} />
                <Route path="wallet" element={<WalletPayments />} />
                <Route path="settings" element={<AccountSettings />} />
                <Route path="edit-profile" element={<EditProfile />} />
                <Route path="saved" element={<SavedWishlist />} />
                <Route path="help" element={<HelpCenter />} />
                <Route path="*" element={<Navigate to="home" replace />} />
              </Routes>
            </CustomerLayout>
          </ProtectedRoute>
        }
      />

      {/* Merchant Protected Pages */}
      <Route
        path="/merchant/*"
        element={
          <ProtectedRoute allowedRole="RESTAURANT">
            <MerchantLayout>
              <Routes>
                <Route path="dashboard" element={<MerchantDashboard />} />
                <Route path="menu" element={<MerchantMenu />} />
                <Route path="menu/edit/:id" element={<MerchantEditItem />} />
                <Route path="orders" element={<MerchantOrders />} />
                <Route path="revenue" element={<MerchantRevenue />} />
                <Route path="profile" element={<MerchantProfile />} />
                <Route path="staff" element={<MerchantStaff />} />
                <Route path="offers" element={<MerchantOffers />} />
                <Route path="reviews" element={<MerchantReviews />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </MerchantLayout>
          </ProtectedRoute>
        }
      />

      {/* Admin Protected Pages */}
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute allowedRole="ADMIN">
            <AdminLayout>
              <Routes>
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="onboarding" element={<AdminOnboarding />} />
                <Route path="analytics" element={<AdminAnalytics />} />
                <Route path="support" element={<AdminSupport />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </AdminLayout>
          </ProtectedRoute>
        }
      />

      {/* Delivery Protected Pages */}
      <Route
        path="/delivery/*"
        element={
          <ProtectedRoute allowedRole="DELIVERY">
            <DeliveryLayout>
              <Routes>
                <Route path="dashboard" element={<DeliveryDashboard />} />
                <Route path="orders" element={<DeliveryOrders />} />
                <Route path="profile" element={<DeliveryProfile />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </DeliveryLayout>
          </ProtectedRoute>
        }
      />

      {/* Fallback 404 Route */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
export default AppRoutes;
