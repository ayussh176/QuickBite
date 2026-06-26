import React from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { RootState } from "../../redux/store";
import { logout } from "../../redux/authSlice";
import toast from "react-hot-toast";

interface MerchantLayoutProps {
  children: React.ReactNode;
}

export const MerchantLayout: React.FC<MerchantLayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    toast.success("Merchant logged out successfully");
    navigate("/");
  };

  const navItems = [
    { label: "Dashboard", path: "/merchant/dashboard", icon: "dashboard" },
    { label: "Menu Management", path: "/merchant/menu", icon: "restaurant_menu" },
    { label: "Order Management", path: "/merchant/orders", icon: "receipt_long" },
    { label: "Revenue Analytics", path: "/merchant/revenue", icon: "insights" },
    { label: "Restaurant Profile", path: "/merchant/profile", icon: "storefront" },
    { label: "Staff Management", path: "/merchant/staff", icon: "group" },
    { label: "Offers & Coupons", path: "/merchant/offers", icon: "local_offer" },
    { label: "Customer Reviews", path: "/merchant/reviews", icon: "star" },
  ];

  return (
    <div className="min-h-screen bg-background flex flex-col font-body-md text-on-surface">
      {/* Top Header */}
      <header className="sticky top-0 z-40 bg-surface-container-lowest border-b border-outline-variant h-16 flex items-center justify-between px-6 shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/merchant/dashboard" className="text-xl font-bold text-primary-container tracking-tight">
            QuickBite Merchant Portal
          </Link>
          <span className="bg-primary-fixed text-on-primary-fixed text-xs font-semibold px-2 py-0.5 rounded-full">
            The Burger House
          </span>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-sm font-medium text-secondary">
            Welcome, {user?.name || "Partner"}
          </span>
          <div className="w-8 h-8 rounded-full bg-primary-container text-white flex items-center justify-center font-bold">
            M
          </div>
        </div>
      </header>

      {/* Main Container with Sidebar */}
      <div className="flex flex-1">
        {/* Sidebar */}
        <aside className="w-64 bg-surface-container-lowest border-r border-outline-variant py-6 flex flex-col justify-between shrink-0">
          <ul className="space-y-1 px-4">
            {navItems.map((item) => {
              const isActive = location.pathname.startsWith(item.path);
              return (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-4 py-2.5 rounded-xl font-medium transition-all ${
                      isActive
                        ? "bg-primary-container text-white shadow-md shadow-primary-container/20"
                        : "text-secondary hover:bg-surface-container hover:text-on-surface"
                    }`}
                  >
                    <span className="material-symbols-outlined">{item.icon}</span>
                    <span>{item.label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>

          <div className="px-4">
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-red-600 hover:bg-red-50 rounded-xl font-medium transition-all"
            >
              <span className="material-symbols-outlined">logout</span>
              <span>Logout</span>
            </button>
          </div>
        </aside>

        {/* Content Body */}
        <main className="flex-1 bg-surface p-8 overflow-y-auto">
          <div className="max-w-7xl mx-auto space-y-6">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};
