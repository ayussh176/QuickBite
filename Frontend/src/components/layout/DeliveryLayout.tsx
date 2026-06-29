import React from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { RootState } from "../../redux/store";
import { logout } from "../../redux/authSlice";
import toast from "react-hot-toast";

interface DeliveryLayoutProps {
  children: React.ReactNode;
}

export const DeliveryLayout: React.FC<DeliveryLayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    toast.success("Delivery Partner logged out successfully");
    navigate("/");
  };

  const navItems = [
    { label: "Delivery Dashboard", path: "/delivery/dashboard", icon: "sports_motorsports" },
    { label: "Order History", path: "/delivery/orders", icon: "history" },
    { label: "Profile & Vehicle", path: "/delivery/profile", icon: "badge" },
  ];

  return (
    <div className="min-h-screen bg-background flex flex-col font-body-md text-on-surface">
      {/* Top Header */}
      <header className="sticky top-0 z-40 bg-surface-container-lowest border-b border-outline-variant h-16 flex items-center justify-between px-6 shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/delivery/dashboard" className="text-xl font-bold text-primary-container tracking-tight">
            QuickBite Courier Portal
          </Link>
          <span className="bg-tertiary-container/10 text-tertiary text-xs font-semibold px-2 py-0.5 rounded-full border border-tertiary/20">
            Courier Online
          </span>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-sm font-medium text-secondary">
            Rider: {user?.name || "Delivery Partner"}
          </span>
          <div className="w-8 h-8 rounded-full bg-tertiary text-white flex items-center justify-center font-bold">
            D
          </div>
        </div>
      </header>

      {/* Main Container with Sidebar */}
      <div className="flex flex-1 h-[calc(100vh-4rem)] overflow-hidden">
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
export default DeliveryLayout;
