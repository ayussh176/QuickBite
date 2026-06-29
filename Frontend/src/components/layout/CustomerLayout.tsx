import React from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import type { RootState } from "../../redux/store";
import { logout } from "../../redux/authSlice";
import { clearCart, setCart } from "../../redux/cartSlice";
import { apiService } from "../../services/api/apiClient";
import toast from "react-hot-toast";

interface CustomerLayoutProps {
  children: React.ReactNode;
}

export const CustomerLayout: React.FC<CustomerLayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);
  const cart = useSelector((state: RootState) => state.cart);

  React.useEffect(() => {
    if (isAuthenticated) {
      apiService.getCart().then((mappedCart) => {
        dispatch(setCart(mappedCart));
      }).catch(err => console.error("Error fetching cart", err));
    }
  }, [isAuthenticated, dispatch]);

  const cartQuantity = cart.items.reduce((acc, item) => acc + item.quantity, 0);

  const handleLogout = () => {
    dispatch(logout());
    dispatch(clearCart());
    toast.success("Logged out successfully");
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-background flex flex-col font-body-md text-on-surface">
      {/* Top Navigation Bar */}
      <nav className="sticky top-0 z-50 flex justify-between items-center w-full px-margin-desktop py-stack-md max-w-container-max mx-auto bg-surface-container-lowest border-b border-outline-variant shadow-sm transition-all duration-300">
        <div className="flex items-center gap-gutter flex-1">
          <Link to={isAuthenticated ? "/customer/home" : "/"} className="text-title-lg font-bold text-primary-container tracking-tight cursor-pointer">
            QuickBite
          </Link>
          
          <div className="hidden md:flex items-center bg-surface-container-low px-4 py-2 rounded-full border border-outline-variant hover:border-primary-container transition-colors cursor-pointer">
            <span className="material-symbols-outlined text-primary-container mr-2">location_on</span>
            <span className="font-body-md text-on-surface truncate max-w-[150px]">Downtown Manhattan, NY</span>
            <span className="material-symbols-outlined text-secondary ml-1">keyboard_arrow_down</span>
          </div>

          <div className="hidden lg:flex flex-1 max-w-xl relative">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-secondary">search</span>
            <input 
              className="w-full pl-12 pr-4 py-2 bg-surface-container-low border border-outline-variant rounded-full focus:ring-2 focus:ring-primary-container focus:outline-none font-body-md transition-all" 
              placeholder="Search for burgers, sushi, or 'The Pizza Place'..." 
              type="text"
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  navigate("/customer/restaurants");
                }
              }}
            />
          </div>
        </div>

        <div className="flex items-center gap-stack-lg ml-gutter">
          <div className="hidden md:flex items-center gap-stack-lg">
            <Link 
              className={`font-body-md transition-colors ${location.pathname === "/customer/restaurants" || location.pathname === "/customer/home" ? "text-primary-container border-b-2 border-primary-container pb-1 font-bold" : "text-secondary font-medium hover:text-primary"}`} 
              to="/customer/restaurants"
            >
              Browse
            </Link>
            <Link 
              className={`font-body-md transition-colors ${location.pathname === "/customer/wallet" ? "text-primary-container border-b-2 border-primary-container pb-1 font-bold" : "text-secondary font-medium hover:text-primary"}`} 
              to="/customer/wallet"
            >
              Wallet (₹{user?.walletBalance ?? 762})
            </Link>
            <Link 
              className={`font-body-md transition-colors ${location.pathname === "/customer/orders" ? "text-primary-container border-b-2 border-primary-container pb-1 font-bold" : "text-secondary font-medium hover:text-primary"}`} 
              to="/customer/orders"
            >
              Orders
            </Link>
          </div>

          <div className="flex items-center gap-stack-md">
            <Link to="/customer/cart" className="p-2 rounded-full hover:bg-surface-container-low transition-colors relative block">
              <span className="material-symbols-outlined text-on-surface">shopping_cart</span>
              {cartQuantity > 0 && (
                <span className="absolute top-0 right-0 bg-primary-container text-white text-[10px] w-4 h-4 flex items-center justify-center rounded-full font-bold">
                  {cartQuantity}
                </span>
              )}
            </Link>
            
            <button className="p-2 rounded-full hover:bg-surface-container-low transition-colors">
              <span className="material-symbols-outlined text-on-surface">notifications</span>
            </button>

            {isAuthenticated ? (
              <div className="relative group">
                <button className="w-10 h-10 rounded-full overflow-hidden border border-outline-variant flex items-center justify-center bg-primary-fixed text-primary font-bold">
                  {user?.name?.slice(0, 2) || "US"}
                </button>
                <div className="absolute right-0 mt-2 w-48 bg-surface-container-lowest rounded-xl shadow-lg border border-outline-variant py-2 hidden group-hover:block z-50">
                  <Link to="/customer/dashboard" className="block px-4 py-2 hover:bg-surface-container-low text-on-surface font-medium">My Dashboard</Link>
                  <Link to="/customer/settings" className="block px-4 py-2 hover:bg-surface-container-low text-on-surface font-medium">Account Settings</Link>
                  <Link to="/customer/help" className="block px-4 py-2 hover:bg-surface-container-low text-on-surface font-medium">Help Center</Link>
                  <hr className="my-1 border-outline-variant" />
                  <button onClick={handleLogout} className="w-full text-left px-4 py-2 hover:bg-red-50 text-red-600 font-medium">Logout</button>
                </div>
              </div>
            ) : (
              <Link to="/auth/login" className="bg-primary-container text-white px-4 py-2 rounded-full font-button text-button hover:bg-primary transition-all">
                Login
              </Link>
            )}
          </div>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="flex-1 max-w-container-max w-full mx-auto px-margin-desktop py-stack-lg">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-surface-container border-t border-outline-variant py-stack-lg mt-auto">
        <div className="max-w-container-max mx-auto px-margin-desktop flex flex-col md:flex-row justify-between items-center gap-stack-md">
          <div className="text-secondary text-body-md">
            &copy; {new Date().getFullYear()} QuickBite Enterprise. All rights reserved.
          </div>
          <div className="flex gap-stack-lg text-secondary font-medium">
            <Link to="/customer/help" className="hover:text-primary-container">Privacy Policy</Link>
            <Link to="/customer/help" className="hover:text-primary-container">Terms of Service</Link>
            <Link to="/customer/help" className="hover:text-primary-container">Contact Support</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};
