import React, { useState, useEffect, useCallback } from "react";
import { Link, useNavigate, useParams, useLocation } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import type { RootState } from "../redux/store";
import { clearCart, setCart } from "../redux/cartSlice";
import { updateWalletBalance } from "../redux/authSlice";
import { apiService } from "../services/api/apiClient";
import type { Restaurant, MenuItem, Order, WalletTransaction } from "../services/api/apiClient";
import { QRCodeSVG } from "qrcode.react";
import toast from "react-hot-toast";

// 1. CUSTOMER HOME
export const CustomerHome: React.FC = () => {
  const navigate = useNavigate();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);

  useEffect(() => {
    apiService.getRestaurants().then(setRestaurants);
  }, []);

  const categories = [
    { name: "Burgers", icon: "🍔" },
    { name: "Pizza", icon: "🍕" },
    { name: "Sushi", icon: "🍣" },
    { name: "Desserts", icon: "🍰" },
    { name: "Biryani", icon: "🍲" },
    { name: "Healthy", icon: "🥗" }
  ];

  return (
    <div className="space-y-8">
      {/* Bento Banner */}
      <section className="bg-primary-container text-white p-8 rounded-2xl flex flex-col md:flex-row justify-between items-center gap-6 shadow-xl relative overflow-hidden">
        <div className="absolute right-0 top-0 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        <div className="space-y-4 max-w-xl">
          <h2 className="text-3xl font-extrabold tracking-tight">Order from your favorite restaurants in seconds.</h2>
          <p className="text-white/90 text-lg">Delicious food delivered hot and fresh directly to your doorstep.</p>
          <button 
            onClick={() => navigate("/customer/restaurants")}
            className="bg-white text-primary-container px-6 py-2.5 rounded-full font-bold shadow-lg hover:scale-105 transition-all text-sm"
          >
            Order Now
          </button>
        </div>
        <div className="hidden md:block text-8xl">🍔</div>
      </section>

      {/* Categories Bento Carousel */}
      <section className="space-y-4">
        <h3 className="text-headline-md font-bold text-on-surface">Explore Categories</h3>
        <div className="grid grid-cols-3 md:grid-cols-6 gap-4">
          {categories.map((cat) => (
            <div 
              key={cat.name}
              onClick={() => navigate("/customer/restaurants")}
              className="bg-surface-container-low hover:bg-surface-container-high transition-all p-6 rounded-2xl text-center cursor-pointer border border-outline-variant group"
            >
              <div className="text-4xl mb-2 group-hover:scale-110 transition-transform">{cat.icon}</div>
              <div className="font-semibold text-sm text-on-surface">{cat.name}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Featured Restaurants List */}
      <section className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-headline-md font-bold text-on-surface">Top Restaurants Near You</h3>
          <Link to="/customer/restaurants" className="text-primary-container font-semibold hover:underline text-sm">
            View All
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter">
          {restaurants.map((rest) => (
            <div 
              key={rest.id} 
              onClick={() => navigate(`/customer/restaurant/${rest.id}`)}
              className="bg-surface-container-lowest border border-outline-variant hover:border-primary-container rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all cursor-pointer flex flex-col sm:flex-row group"
            >
              <div className="sm:w-48 h-40 sm:h-auto overflow-hidden relative">
                <img src={rest.imageUrl} alt={rest.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                <span className="absolute top-3 left-3 bg-primary-container text-white text-xs font-extrabold px-2 py-0.5 rounded-full">
                  {rest.offerText}
                </span>
              </div>
              <div className="p-6 flex-1 flex flex-col justify-between">
                <div>
                  <h4 className="text-title-lg font-bold text-on-surface group-hover:text-primary-container transition-colors mb-1">
                    {rest.name}
                  </h4>
                  <p className="text-secondary text-sm mb-3">{rest.cuisine.join(", ")}</p>
                </div>
                <div className="flex items-center gap-4 text-xs font-semibold text-secondary">
                  <span className="flex items-center gap-1 bg-tertiary-container/10 text-tertiary px-2 py-0.5 rounded-full">
                    <span className="material-symbols-outlined text-xs">star</span>
                    {rest.rating}
                  </span>
                  <span>{rest.deliveryTime}</span>
                  <span>{rest.distance}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

// 2. BROWSE RESTAURANTS
export const BrowseRestaurants: React.FC = () => {
  const navigate = useNavigate();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [search, setSearch] = useState("");
  const [filterVeg, setFilterVeg] = useState(false);
  const [filterRating, setFilterRating] = useState(false);

  useEffect(() => {
    apiService.getRestaurants().then(setRestaurants);
  }, []);

  const filtered = restaurants.filter((r) => {
    const matchesSearch = r.name.toLowerCase().includes(search.toLowerCase()) || 
                          r.cuisine.some(c => c.toLowerCase().includes(search.toLowerCase()));
    const matchesVeg = !filterVeg || r.isVeg;
    const matchesRating = !filterRating || r.rating >= 4.5;
    return matchesSearch && matchesVeg && matchesRating;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h2 className="text-headline-md font-bold text-on-surface">Browse Restaurants</h2>
          <p className="text-secondary">Discover delicious choices near you</p>
        </div>

        {/* Search Bar */}
        <div className="w-full md:w-80 relative">
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-secondary">search</span>
          <input 
            type="text" 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search cuisines or dishes..."
            className="w-full pl-12 pr-4 py-2 bg-surface-container-low border border-outline-variant rounded-full focus:outline-none focus:border-primary-container"
          />
        </div>
      </div>

      {/* Filter Chips */}
      <div className="flex items-center gap-2 border-b border-outline-variant pb-4 overflow-x-auto">
        <button 
          onClick={() => setFilterVeg(!filterVeg)}
          className={`px-4 py-1.5 rounded-full text-xs font-bold border transition-all ${
            filterVeg 
              ? "bg-tertiary-container/10 border-tertiary text-tertiary" 
              : "border-outline-variant hover:bg-surface-container-low text-secondary"
          }`}
        >
          Pure Veg 🥗
        </button>
        <button 
          onClick={() => setFilterRating(!filterRating)}
          className={`px-4 py-1.5 rounded-full text-xs font-bold border transition-all ${
            filterRating 
              ? "bg-primary-container/10 border-primary-container text-primary-container" 
              : "border-outline-variant hover:bg-surface-container-low text-secondary"
          }`}
        >
          Rating 4.5+ ⭐
        </button>
      </div>

      {/* Restaurant List Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {filtered.map((rest) => (
          <div 
            key={rest.id}
            onClick={() => navigate(`/customer/restaurant/${rest.id}`)}
            className="bg-surface-container-lowest border border-outline-variant hover:border-primary-container rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all cursor-pointer group flex flex-col h-full"
          >
            <div className="h-48 overflow-hidden relative">
              <img src={rest.imageUrl} alt={rest.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
              <span className="absolute top-3 left-3 bg-primary-container text-white text-xs font-extrabold px-2 py-0.5 rounded-full">
                {rest.offerText}
              </span>
            </div>
            <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
              <div>
                <div className="flex items-center justify-between mb-1">
                  <h4 className="text-title-md font-bold text-on-surface group-hover:text-primary-container transition-colors truncate">
                    {rest.name}
                  </h4>
                  <span className="flex items-center gap-0.5 bg-tertiary-container/10 text-tertiary text-xs font-bold px-2 py-0.5 rounded-full">
                    {rest.rating} ★
                  </span>
                </div>
                <p className="text-secondary text-xs truncate">{rest.cuisine.join(", ")}</p>
              </div>
              <div className="flex items-center justify-between text-xs font-semibold text-secondary pt-3 border-t border-outline-variant">
                <span>{rest.deliveryTime}</span>
                <span>{rest.distance}</span>
                <span>₹{rest.costForTwo} for two</span>
              </div>
            </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="col-span-full text-center py-12 text-secondary">
            No restaurants found matching your filters.
          </div>
        )}
      </div>
    </div>
  );
};

// 3. RESTAURANT DETAILS
export const RestaurantDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const location = useLocation();
  const [restaurant, setRestaurant] = useState<Restaurant | undefined>(undefined);
  const [selectedItem, setSelectedItem] = useState<MenuItem | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [selectedPatty, setSelectedPatty] = useState("Single Patty");
  const [selectedExtras, setSelectedExtras] = useState<string[]>([]);
  const cart = useSelector((state: RootState) => state.cart);

  useEffect(() => {
    if (id) {
      apiService.getRestaurantById(id).then(setRestaurant);
    }
  }, [id]);

  if (!restaurant) {
    return <div className="text-center py-12 text-secondary">Loading restaurant details...</div>;
  }

  const handleCustomizationToggle = (choiceName: string) => {
    if (selectedExtras.includes(choiceName)) {
      setSelectedExtras(selectedExtras.filter((e) => e !== choiceName));
    } else {
      setSelectedExtras([...selectedExtras, choiceName]);
    }
  };

  const handleAddToCart = () => {
    if (!selectedItem) return;

    // Calculate customization pricing
    let extraPrice = 0;
    const choicesList: string[] = [];

    const pattyOption = selectedItem.customizationOptions?.find(o => o.title === "Choose Size");
    if (pattyOption) {
      const choice = pattyOption.choices.find(c => c.name === selectedPatty);
      if (choice) {
        extraPrice += choice.price;
        if (choice.price > 0) choicesList.push(`${choice.name} (+₹${choice.price})`);
      }
    }

    const extrasOption = selectedItem.customizationOptions?.find(o => o.title === "Add Extras");
    if (extrasOption) {
      selectedExtras.forEach((extra) => {
        const choice = extrasOption.choices.find(c => c.name === extra);
        if (choice) {
          extraPrice += choice.price;
          choicesList.push(`${choice.name} (+₹${choice.price})`);
        }
      });
    }

    apiService.addCartItem(selectedItem.id, quantity, choicesList.join(",")).then((mappedCart) => {
      dispatch(setCart(mappedCart));
    });

    toast.success(`${selectedItem.name} added to cart`);
    setSelectedItem(null); // close dialog
    setQuantity(1);
    setSelectedExtras([]);
  };

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div 
        className="h-64 rounded-2xl overflow-hidden relative flex items-end p-8 bg-cover bg-center" 
        style={{ backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.1)), url(${restaurant.bannerUrl})` }}
      >
        <div className="text-white space-y-2">
          <h2 className="text-3xl font-extrabold">{restaurant.name}</h2>
          <p className="opacity-90 text-sm">{restaurant.cuisine.join(", ")} &bull; {restaurant.address}</p>
          <div className="flex items-center gap-4 text-xs font-bold pt-2">
            <span className="bg-tertiary-container text-white px-2 py-0.5 rounded-full">{restaurant.rating} ★</span>
            <span>{restaurant.deliveryTime}</span>
            <span>{restaurant.distance}</span>
          </div>
        </div>
      </div>

      {/* Nav tabs */}
      <div className="flex gap-4 border-b border-outline-variant pb-2">
        <Link 
          to={`/customer/restaurant/${id}`} 
          className={`${location.pathname === `/customer/restaurant/${id}` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Menu
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/about`} 
          className={`${location.pathname === `/customer/restaurant/${id}/about` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          About
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/reviews`} 
          className={`${location.pathname === `/customer/restaurant/${id}/reviews` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Reviews
        </Link>
      </div>

      {/* Menu & Cart layout */}
      <div className="flex flex-col lg:flex-row gap-gutter">
        {/* Menu Items */}
        <div className="flex-1 space-y-6">
          <h3 className="text-headline-md font-bold text-on-surface">Main Menu</h3>
          <div className="space-y-4">
            {restaurant.menu.map((item) => (
              <div 
                key={item.id} 
                className="bg-surface-container-lowest border border-outline-variant p-4 rounded-xl flex justify-between gap-4"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex items-center gap-2">
                    <span className={`w-4 h-4 border flex items-center justify-center text-[8px] font-bold ${item.isVeg ? "border-green-600 text-green-600" : "border-red-600 text-red-600"}`}>
                      {item.isVeg ? "🟢" : "🔴"}
                    </span>
                    {item.isBestSeller && (
                      <span className="bg-primary-container/10 text-primary-container text-[10px] font-extrabold px-2 py-0.5 rounded-full">
                        Bestseller
                      </span>
                    )}
                  </div>
                  <h4 className="font-bold text-on-surface text-lg">{item.name}</h4>
                  <p className="text-secondary text-sm line-clamp-2">{item.description}</p>
                  <div className="font-bold text-on-surface">₹{item.price}</div>
                </div>

                <div className="w-28 flex flex-col items-center gap-2">
                  <div className="w-24 h-24 rounded-lg overflow-hidden border border-outline-variant">
                    <img src={item.image} alt={item.name} className="w-full h-full object-cover" />
                  </div>
                  <button 
                    onClick={() => {
                      if (item.customizable) {
                        setSelectedItem(item);
                      } else {
                        apiService.addCartItem(item.id, 1).then((mappedCart) => {
                          dispatch(setCart(mappedCart));
                          toast.success(`${item.name} added to cart`);
                        });
                      }
                    }}
                    className="bg-white border border-primary-container text-primary-container font-extrabold px-6 py-1 rounded-full text-xs hover:bg-primary-container/5 transition-all shadow-sm"
                  >
                    ADD
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Mini Cart Sidebar */}
        {cart.items.length > 0 && (
          <aside className="w-full lg:w-80 bg-surface-container-lowest border border-outline-variant rounded-2xl p-6 h-fit space-y-4">
            <h3 className="font-bold text-lg text-on-surface">Your Cart</h3>
            <div className="divide-y divide-outline-variant">
              {cart.items.map((cartItem) => (
                <div key={cartItem.id} className="py-3 flex justify-between items-start gap-2">
                  <div className="space-y-1">
                    <div className="font-semibold text-sm text-on-surface">{cartItem.name}</div>
                    {cartItem.customizations && cartItem.customizations.map((c, i) => (
                      <div key={i} className="text-xs text-secondary">{c}</div>
                    ))}
                    <div className="text-xs font-bold text-on-surface">₹{cartItem.price * cartItem.quantity}</div>
                  </div>
                  <div className="flex items-center gap-2 bg-surface border border-outline-variant rounded-full px-2 py-0.5 text-xs font-bold">
                    <button onClick={() => {
                      if (cartItem.quantity > 1) {
                        apiService.updateCartItem(cartItem.id, cartItem.quantity - 1).then((mappedCart) => dispatch(setCart(mappedCart)));
                      } else {
                        apiService.removeCartItem(cartItem.id).then((mappedCart) => dispatch(setCart(mappedCart)));
                      }
                    }}>-</button>
                    <span>{cartItem.quantity}</span>
                    <button onClick={() => {
                      apiService.updateCartItem(cartItem.id, cartItem.quantity + 1).then((mappedCart) => dispatch(setCart(mappedCart)));
                    }}>+</button>
                  </div>
                </div>
              ))}
            </div>
            <button 
              onClick={() => navigate("/customer/checkout")}
              className="w-full bg-primary-container text-white py-2.5 rounded-full font-button font-bold text-center hover:bg-primary transition-all block"
            >
              Checkout (₹{cart.items.reduce((acc, i) => acc + i.price * i.quantity, 0)})
            </button>
          </aside>
        )}
      </div>

      {/* Customization Dialog modal overlay */}
      {selectedItem && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-surface-container-lowest rounded-2xl border border-outline-variant w-full max-w-lg p-6 space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-bold text-xl text-on-surface">{selectedItem.name}</h3>
                <p className="text-secondary text-sm">Customize your selection</p>
              </div>
              <button onClick={() => setSelectedItem(null)} className="material-symbols-outlined text-secondary">close</button>
            </div>

            {/* Sizes */}
            {selectedItem.customizationOptions?.find((o) => o.title === "Choose Size") && (
              <div className="space-y-2">
                <h4 className="font-semibold text-sm text-on-surface">Choose Size</h4>
                <div className="space-y-1">
                  {selectedItem.customizationOptions?.find((o) => o.title === "Choose Size")?.choices.map((c) => (
                    <label key={c.name} className="flex justify-between items-center p-3 border border-outline-variant rounded-xl cursor-pointer hover:bg-surface-container-low">
                      <div className="flex items-center gap-2">
                        <input 
                          type="radio" 
                          name="size"
                          checked={selectedPatty === c.name} 
                          onChange={() => setSelectedPatty(c.name)}
                          className="text-primary-container focus:ring-primary-container"
                        />
                        <span className="text-sm font-medium">{c.name}</span>
                      </div>
                      <span className="text-sm font-bold">+₹{c.price}</span>
                    </label>
                  ))}
                </div>
              </div>
            )}

            {/* Extras */}
            {selectedItem.customizationOptions?.find((o) => o.title === "Add Extras") && (
              <div className="space-y-2">
                <h4 className="font-semibold text-sm text-on-surface">Add Extras</h4>
                <div className="space-y-1">
                  {selectedItem.customizationOptions?.find((o) => o.title === "Add Extras")?.choices.map((c) => (
                    <label key={c.name} className="flex justify-between items-center p-3 border border-outline-variant rounded-xl cursor-pointer hover:bg-surface-container-low">
                      <div className="flex items-center gap-2">
                        <input 
                          type="checkbox" 
                          checked={selectedExtras.includes(c.name)}
                          onChange={() => handleCustomizationToggle(c.name)}
                          className="text-primary-container rounded focus:ring-primary-container"
                        />
                        <span className="text-sm font-medium">{c.name}</span>
                      </div>
                      <span className="text-sm font-bold">+₹{c.price}</span>
                    </label>
                  ))}
                </div>
              </div>
            )}

            <div className="flex justify-between items-center pt-4 border-t border-outline-variant">
              <div className="flex items-center gap-3 bg-surface border border-outline-variant rounded-full px-3 py-1 font-bold">
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))}>-</button>
                <span>{quantity}</span>
                <button onClick={() => setQuantity(quantity + 1)}>+</button>
              </div>
              <button 
                onClick={handleAddToCart}
                className="bg-primary-container text-white px-6 py-2.5 rounded-full font-button font-bold hover:bg-primary transition-all"
              >
                Add to Cart
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// 4. ABOUT RESTAURANT
export const AboutRestaurant: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [restaurant, setRestaurant] = useState<Restaurant | undefined>(undefined);
  const location = useLocation();

  useEffect(() => {
    if (id) {
      apiService.getRestaurantById(id).then(setRestaurant);
    }
  }, [id]);

  if (!restaurant) return <div className="text-center py-12 text-secondary">Loading...</div>;

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div 
        className="h-64 rounded-2xl overflow-hidden relative flex items-end p-8 bg-cover bg-center" 
        style={{ backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.1)), url(${restaurant.bannerUrl})` }}
      >
        <div className="text-white space-y-2">
          <h2 className="text-3xl font-extrabold">{restaurant.name}</h2>
          <p className="opacity-90 text-sm">{restaurant.cuisine.join(", ")} &bull; {restaurant.address}</p>
          <div className="flex items-center gap-4 text-xs font-bold pt-2">
            <span className="bg-tertiary-container text-white px-2 py-0.5 rounded-full">{restaurant.rating} ★</span>
            <span>{restaurant.deliveryTime}</span>
            <span>{restaurant.distance}</span>
          </div>
        </div>
      </div>

      {/* Nav tabs */}
      <div className="flex gap-4 border-b border-outline-variant pb-2">
        <Link 
          to={`/customer/restaurant/${id}`} 
          className={`${location.pathname === `/customer/restaurant/${id}` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Menu
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/about`} 
          className={`${location.pathname === `/customer/restaurant/${id}/about` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          About
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/reviews`} 
          className={`${location.pathname === `/customer/restaurant/${id}/reviews` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Reviews
        </Link>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
        <p className="text-on-surface font-semibold">Address:</p>
        <p className="text-secondary">{restaurant.address}</p>
        <p className="text-on-surface font-semibold">Opening Hours:</p>
        <p className="text-secondary">10:00 AM - 11:30 PM (Daily)</p>
        <p className="text-on-surface font-semibold">FSSAI License:</p>
        <p className="text-secondary">FSSAI-120938402948</p>
      </div>
    </div>
  );
};

// 5. RESTAURANT REVIEWS
export const RestaurantReviews: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [restaurant, setRestaurant] = useState<Restaurant | undefined>(undefined);
  const location = useLocation();

  useEffect(() => {
    if (id) {
      apiService.getRestaurantById(id).then(setRestaurant);
    }
  }, [id]);

  if (!restaurant) return <div className="text-center py-12 text-secondary">Loading...</div>;

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div 
        className="h-64 rounded-2xl overflow-hidden relative flex items-end p-8 bg-cover bg-center" 
        style={{ backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.1)), url(${restaurant.bannerUrl})` }}
      >
        <div className="text-white space-y-2">
          <h2 className="text-3xl font-extrabold">{restaurant.name}</h2>
          <p className="opacity-90 text-sm">{restaurant.cuisine.join(", ")} &bull; {restaurant.address}</p>
          <div className="flex items-center gap-4 text-xs font-bold pt-2">
            <span className="bg-tertiary-container text-white px-2 py-0.5 rounded-full">{restaurant.rating} ★</span>
            <span>{restaurant.deliveryTime}</span>
            <span>{restaurant.distance}</span>
          </div>
        </div>
      </div>

      {/* Nav tabs */}
      <div className="flex gap-4 border-b border-outline-variant pb-2">
        <Link 
          to={`/customer/restaurant/${id}`} 
          className={`${location.pathname === `/customer/restaurant/${id}` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Menu
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/about`} 
          className={`${location.pathname === `/customer/restaurant/${id}/about` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          About
        </Link>
        <Link 
          to={`/customer/restaurant/${id}/reviews`} 
          className={`${location.pathname === `/customer/restaurant/${id}/reviews` ? "text-primary-container border-b-2 border-primary-container pb-2 font-bold" : "text-secondary font-semibold hover:text-primary pb-2"}`}
        >
          Reviews
        </Link>
      </div>

      <div className="space-y-4">
        {restaurant.reviews.map((rev) => (
          <div key={rev.id} className="bg-surface-container-lowest border border-outline-variant p-5 rounded-2xl space-y-2">
            <div className="flex justify-between items-center">
              <span className="font-bold text-on-surface">{rev.userName}</span>
              <span className="text-xs text-secondary">{rev.date}</span>
            </div>
            <div className="text-xs font-bold text-tertiary">{rev.rating} ★ Rating</div>
            <p className="text-secondary text-sm">{rev.comment}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

// 6. SHOPPING CART
export const ShoppingCart: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const cart = useSelector((state: RootState) => state.cart);

  if (cart.items.length === 0) {
    return (
      <div className="text-center py-20 space-y-4">
        <span className="material-symbols-outlined text-6xl text-secondary">shopping_cart</span>
        <h2 className="text-2xl font-bold text-on-surface">Your Cart is Empty</h2>
        <p className="text-secondary max-w-sm mx-auto">Explore top-rated restaurants nearby to fill your cart with delicious meals.</p>
        <Link to="/customer/restaurants" className="inline-block bg-primary-container text-white px-6 py-2.5 rounded-full font-button font-bold hover:bg-primary">Browse Restaurants</Link>
      </div>
    );
  }

  const subtotal = cart.items.reduce((acc, i) => acc + i.price * i.quantity, 0);

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Your Shopping Cart</h2>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        <div className="lg:col-span-2 bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
          {cart.items.map((i) => (
            <div key={i.id} className="flex justify-between items-center py-4 border-b border-outline-variant last:border-0">
              <div>
                <h4 className="font-bold text-on-surface">{i.name}</h4>
                {i.customizations && i.customizations.map((c, idx) => (
                  <div key={idx} className="text-xs text-secondary">{c}</div>
                ))}
                <div className="font-bold text-sm text-primary-container">₹{i.price}</div>
              </div>
              <div className="flex items-center gap-3 bg-surface border border-outline-variant rounded-full px-3 py-1 font-bold text-sm">
                <button onClick={() => {
                  if (i.quantity > 1) {
                    apiService.updateCartItem(i.id, i.quantity - 1).then((mappedCart) => dispatch(setCart(mappedCart)));
                  } else {
                    apiService.removeCartItem(i.id).then((mappedCart) => dispatch(setCart(mappedCart)));
                  }
                }}>-</button>
                <span>{i.quantity}</span>
                <button onClick={() => {
                  apiService.updateCartItem(i.id, i.quantity + 1).then((mappedCart) => dispatch(setCart(mappedCart)));
                }}>+</button>
              </div>
            </div>
          ))}
        </div>

        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-6 h-fit">
          <h3 className="font-bold text-lg text-on-surface">Order Summary</h3>
          <div className="space-y-2 text-sm text-secondary font-medium">
            <div className="flex justify-between"><span>Subtotal</span><span className="text-on-surface font-bold">₹{subtotal}</span></div>
            <div className="flex justify-between"><span>Delivery Fee</span><span className="text-on-surface font-bold">₹30</span></div>
            <div className="flex justify-between"><span>GST & Tax</span><span className="text-on-surface font-bold">₹15</span></div>
          </div>
          <hr className="border-outline-variant" />
          <div className="flex justify-between font-bold text-lg text-on-surface">
            <span>Total</span>
            <span>₹{subtotal + 45}</span>
          </div>
          <button 
            onClick={() => navigate("/customer/checkout")}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button font-bold text-center hover:bg-primary transition-all"
          >
            Proceed to Checkout
          </button>
        </div>
      </div>
    </div>
  );
};

// 7. CHECKOUT
export const Checkout: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const cart = useSelector((state: RootState) => state.cart);
  const { user } = useSelector((state: RootState) => state.auth);
  const [address, setAddress] = useState("H-15, Sector 44, Noida, UP, 201301");
  const [paymentMode, setPaymentMode] = useState<"wallet" | "qr">("wallet");

  const subtotal = cart.items.reduce((acc, i) => acc + i.price * i.quantity, 0);
  const discount = cart.appliedCoupon ? (cart.appliedCoupon.type === "percentage" ? (subtotal * cart.appliedCoupon.discount) / 100 : cart.appliedCoupon.discount) : 0;
  const total = subtotal + 30 + 15 - discount;

  const handlePlaceOrder = async () => {
    if (paymentMode === "wallet") {
      if ((user?.walletBalance ?? 0) < total) {
        toast.error("Insufficient wallet balance. Please add funds or pay via QR Code.");
        return;
      }
      
      const newBalance = (user?.walletBalance ?? 0) - total;
      dispatch(updateWalletBalance(newBalance));
      
      // Save order
      await apiService.createOrder({
        restaurantId: cart.restaurantId!,
        restaurantName: cart.restaurantName!,
        restaurantImage: cart.restaurantImage!,
        items: cart.items.map((i) => ({ id: i.id, name: i.name, price: i.price, quantity: i.quantity, customizations: i.customizations })),
        subtotal,
        tax: 15,
        deliveryFee: 30,
        discount,
        total,
        address
      });

      await apiService.clearCart().then((mappedCart) => dispatch(setCart(mappedCart)));
      toast.success("Order Placed Successfully!");
      navigate("/customer/order-confirmed");
    } else {
      // Navigate to QR code payment screen with checkout state details
      navigate("/customer/payment", { state: { total, address, subtotal, discount } });
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Checkout</h2>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        <div className="lg:col-span-2 space-y-6">
          {/* Address Panel */}
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-3">
            <h3 className="font-bold text-lg text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary-container">location_on</span>
              Delivery Address
            </h3>
            <textarea 
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full p-3 border border-outline-variant rounded-xl focus:outline-none focus:border-primary-container text-sm"
              rows={2}
            />
          </div>

          {/* Payment Options */}
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
            <h3 className="font-bold text-lg text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary-container">payments</span>
              Select Payment Method
            </h3>
            
            <div className="space-y-2">
              <label className="flex justify-between items-center p-4 border border-outline-variant rounded-xl cursor-pointer hover:bg-surface-container-low">
                <div className="flex items-center gap-3">
                  <input 
                    type="radio" 
                    name="payment" 
                    checked={paymentMode === "wallet"}
                    onChange={() => setPaymentMode("wallet")}
                    className="text-primary-container focus:ring-primary-container"
                  />
                  <div>
                    <div className="font-semibold text-sm">QuickBite Wallet Balance</div>
                    <div className="text-xs text-secondary">Available balance: ₹{user?.walletBalance ?? 762}</div>
                  </div>
                </div>
                <span className="font-bold text-sm text-on-surface">Wallet</span>
              </label>

              <label className="flex justify-between items-center p-4 border border-outline-variant rounded-xl cursor-pointer hover:bg-surface-container-low">
                <div className="flex items-center gap-3">
                  <input 
                    type="radio" 
                    name="payment" 
                    checked={paymentMode === "qr"}
                    onChange={() => setPaymentMode("qr")}
                    className="text-primary-container focus:ring-primary-container"
                  />
                  <div>
                    <div className="font-semibold text-sm">UPI / QR Code Scan</div>
                    <div className="text-xs text-secondary">Generate secure checkout QR code</div>
                  </div>
                </div>
                <span className="font-bold text-sm text-on-surface">Scan QR</span>
              </label>
            </div>
          </div>
        </div>

        {/* Invoice Summary */}
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-6 h-fit">
          <h3 className="font-bold text-lg text-on-surface">Payment Summary</h3>
          <div className="space-y-2 text-sm text-secondary font-medium">
            <div className="flex justify-between"><span>Subtotal</span><span className="text-on-surface font-bold">₹{subtotal}</span></div>
            <div className="flex justify-between"><span>Delivery Charge</span><span className="text-on-surface font-bold">₹30</span></div>
            <div className="flex justify-between"><span>Tax & GST</span><span className="text-on-surface font-bold">₹15</span></div>
            {discount > 0 && <div className="flex justify-between text-tertiary font-bold"><span>Discount Applied</span><span>-₹{discount}</span></div>}
          </div>
          <hr className="border-outline-variant" />
          <div className="flex justify-between font-bold text-lg text-on-surface">
            <span>To Pay</span>
            <span>₹{total}</span>
          </div>
          <button 
            onClick={handlePlaceOrder}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button font-bold text-center hover:bg-primary transition-all"
          >
            {paymentMode === "wallet" ? "Pay & Place Order" : "Generate Checkout QR"}
          </button>
        </div>
      </div>
    </div>
  );
};

// 8. SECURE PAYMENT (QR Code)
export const SecurePayment: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const cart = useSelector((state: RootState) => state.cart);
  const { state } = useLocation();

  const total = state?.total ?? 323;

  const handleSimulatePayment = async () => {
    toast.loading("Simulating transaction response...", { duration: 1500 });
    
    setTimeout(async () => {
      // Save order
      await apiService.createOrder({
        restaurantId: cart.restaurantId || "a8bd6caddd574bae9da4e934e5887ede",
        restaurantName: cart.restaurantName || "The Burger House",
        restaurantImage: cart.restaurantImage || "",
        items: cart.items.map((i) => ({ id: i.id, name: i.name, price: i.price, quantity: i.quantity, customizations: i.customizations })),
        subtotal: state?.subtotal ?? total - 45,
        tax: 15,
        deliveryFee: 30,
        discount: state?.discount ?? 0,
        total,
        address: state?.address || "H-15, Sector 44, Noida, UP, 201301"
      });

      dispatch(clearCart());
      toast.dismiss();
      toast.success("UPI Payment Received successfully!");
      navigate("/customer/order-confirmed");
    }, 1800);
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl text-center space-y-6">
        <h2 className="text-headline-lg font-bold text-on-surface">Scan & Pay</h2>
        <p className="text-secondary text-sm">Scan the dynamic QR code with any UPI app (GPay, PhonePe, Paytm, etc.) to complete payment.</p>
        
        <div className="border border-outline-variant p-4 bg-white rounded-xl inline-block shadow-inner mx-auto">
          <QRCodeSVG value={`upi://pay?pa=quickbite@upi&pn=QuickBite%20Delivery&am=${total}&cu=INR`} size={200} />
        </div>

        <div className="text-lg font-bold text-on-surface">
          Amount: <span className="text-primary-container">₹{total}</span>
        </div>

        <div className="flex flex-col gap-2 max-w-xs mx-auto">
          <button 
            onClick={handleSimulatePayment}
            className="w-full bg-primary-container text-white py-2.5 rounded-full font-button font-bold hover:bg-primary transition-all"
          >
            Simulate Payment Success
          </button>
          <button 
            onClick={() => navigate("/customer/checkout")}
            className="w-full bg-white border border-outline-variant text-on-surface py-2.5 rounded-full font-button font-semibold hover:bg-surface-container-low transition-all"
          >
            Cancel Transaction
          </button>
        </div>
      </div>
    </div>
  );
};

// 9. LIVE ORDER TRACKING
export const OrderTracking: React.FC = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(1); // 1: preparing, 2: out, 3: delivered

  useEffect(() => {
    const timer1 = setTimeout(() => setActiveStep(2), 5000);
    const timer2 = setTimeout(() => {
      setActiveStep(3);
      navigate("/customer/order-delivered");
    }, 12000);

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
    };
  }, [navigate]);

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Track Your Order</h2>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        {/* Mock Map Layout */}
        <div className="lg:col-span-2 bg-surface-container-low border border-outline-variant rounded-2xl h-96 relative flex items-center justify-center overflow-hidden">
          <div className="absolute inset-0 bg-neutral-100 flex flex-col justify-center items-center text-secondary select-none font-bold">
            <span className="material-symbols-outlined text-6xl text-primary-container mb-2">map</span>
            <span>Interactive Tracking Map HUD</span>
            <span className="text-xs font-normal">Mock Google Maps integration active</span>
          </div>

          {/* Delivery Marker */}
          <div className={`absolute transition-all duration-[5000ms] ease-in-out ${
            activeStep === 1 ? "top-1/4 left-1/4" : activeStep === 2 ? "top-1/2 left-1/2" : "top-3/4 left-3/4"
          } bg-primary-container text-white w-10 h-10 rounded-full flex items-center justify-center shadow-lg border-2 border-white animate-bounce`}>
            <span className="material-symbols-outlined text-sm">delivery_dining</span>
          </div>
        </div>

        {/* Tracking Sidebar Timeline */}
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-6 h-fit">
          <h3 className="font-bold text-lg text-on-surface">Delivery Progress</h3>
          <div className="relative border-l-2 border-outline-variant pl-6 space-y-6">
            <div className="relative">
              <span className={`absolute -left-[31px] top-0 w-4 h-4 rounded-full border-2 ${activeStep >= 1 ? "bg-tertiary border-tertiary" : "bg-white border-outline"}`} />
              <h4 className="font-bold text-sm text-on-surface">Order Confirmed</h4>
              <p className="text-xs text-secondary">We've received your order.</p>
            </div>
            <div className="relative">
              <span className={`absolute -left-[31px] top-0 w-4 h-4 rounded-full border-2 ${activeStep >= 1 ? "bg-tertiary border-tertiary" : "bg-white border-outline"}`} />
              <h4 className="font-bold text-sm text-on-surface">Preparing Food</h4>
              <p className="text-xs text-secondary">The kitchen is cooking your delicious meal.</p>
            </div>
            <div className="relative">
              <span className={`absolute -left-[31px] top-0 w-4 h-4 rounded-full border-2 ${activeStep >= 2 ? "bg-tertiary border-tertiary" : "bg-white border-outline"}`} />
              <h4 className="font-bold text-sm text-on-surface">Out for Delivery</h4>
              <p className="text-xs text-secondary">Ramesh Sharma has picked up your food.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// 10. ORDER CONFIRMED
export const OrderConfirmed: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl text-center space-y-6">
        <div className="w-16 h-16 bg-tertiary-container text-white rounded-full flex items-center justify-center mx-auto text-3xl">
          <span className="material-symbols-outlined text-4xl">done</span>
        </div>
        <h2 className="text-headline-lg font-bold text-on-surface">Order Confirmed!</h2>
        <p className="text-secondary text-sm">Your order has been placed successfully. You can track its live delivery status below.</p>
        
        <div className="flex flex-col gap-2 max-w-xs mx-auto">
          <button 
            onClick={() => navigate("/customer/order-tracking")}
            className="w-full bg-primary-container text-white py-2.5 rounded-full font-button font-bold hover:bg-primary transition-all"
          >
            Track My Order
          </button>
          <button 
            onClick={() => navigate("/customer/home")}
            className="w-full bg-white border border-outline-variant text-on-surface py-2.5 rounded-full font-button font-semibold hover:bg-surface-container-low transition-all"
          >
            Go to Homepage
          </button>
        </div>
      </div>
    </div>
  );
};

// 11. ORDER DELIVERED
export const OrderDelivered: React.FC = () => {
  const navigate = useNavigate();
  const [rating, setRating] = useState(5);

  const handleReviewSubmit = () => {
    toast.success("Thank you for your rating!");
    navigate("/customer/home");
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl text-center space-y-6">
        <div className="w-16 h-16 bg-tertiary-container text-white rounded-full flex items-center justify-center mx-auto text-3xl">
          <span className="material-symbols-outlined text-4xl">sports_motorsports</span>
        </div>
        <h2 className="text-headline-lg font-bold text-on-surface">Order Delivered!</h2>
        <p className="text-secondary text-sm">Ramesh has successfully delivered your food. Hope you enjoy your meal!</p>
        
        <div className="space-y-2">
          <label className="block text-sm font-semibold text-on-surface">Rate your Delivery Partner</label>
          <div className="flex justify-center gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button 
                key={star}
                onClick={() => setRating(star)}
                className={`text-3xl ${rating >= star ? "text-primary-container" : "text-neutral-300"}`}
              >
                ★
              </button>
            ))}
          </div>
        </div>

        <button 
          onClick={handleReviewSubmit}
          className="w-full bg-primary-container text-white py-2.5 rounded-full font-button font-bold hover:bg-primary transition-all"
        >
          Submit Review
        </button>
      </div>
    </div>
  );
};

// 12. CUSTOMER DASHBOARD
export const CustomerDashboard: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">My Dashboard</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
        <h3 className="font-bold text-lg text-on-surface">Profile Overview</h3>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-secondary font-medium">Name:</span> <p className="font-semibold">{user?.name || "Customer"}</p></div>
          <div><span className="text-secondary font-medium">Email:</span> <p className="font-semibold">{user?.email || "ayush@gmail.com"}</p></div>
          <div><span className="text-secondary font-medium">Wallet Balance:</span> <p className="font-bold text-primary-container">₹{user?.walletBalance ?? 762}</p></div>
          <div><span className="text-secondary font-medium">Current Role:</span> <p className="font-semibold">{user?.role || "CUSTOMER"}</p></div>
        </div>
      </div>
    </div>
  );
};

// 13. ORDER HISTORY
export const OrderHistory: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    apiService.getOrders().then(setOrders);
  }, []);

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Order History</h2>
      <div className="space-y-4">
        {orders.map((o) => (
          <div key={o.id} className="bg-surface-container-lowest border border-outline-variant p-5 rounded-2xl flex justify-between items-center gap-4">
            <div className="space-y-2">
              <div className="font-bold text-on-surface">{o.restaurantName}</div>
              <div className="text-xs text-secondary">{o.date} &bull; ₹{o.total}</div>
              <div className="text-xs font-semibold text-tertiary">Status: {o.status.toUpperCase()}</div>
            </div>
            <button 
              onClick={() => navigate(`/customer/order/${o.id}`)}
              className="bg-white border border-outline-variant text-on-surface px-4 py-2 rounded-full font-bold text-xs hover:bg-surface-container-low"
            >
              View Receipt
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

// 14. ORDER DETAILS
export const OrderDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | undefined>(undefined);

  useEffect(() => {
    if (id) {
      apiService.getOrderById(id).then(setOrder);
    }
  }, [id]);

  useEffect(() => {
    const handleOrderUpdate = (e: Event) => {
      const customEvent = e as CustomEvent;
      if (customEvent.detail && String(customEvent.detail.orderId) === String(id)) {
        console.log("Real-time order update received. Refreshing...");
        apiService.getOrderById(id!).then(setOrder);
      }
    };

    window.addEventListener("orderUpdate", handleOrderUpdate);
    return () => {
      window.removeEventListener("orderUpdate", handleOrderUpdate);
    };
  }, [id]);

  if (!order) return <div className="text-center py-12 text-secondary">Loading order details...</div>;

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Receipt for #{order.id}</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 max-w-xl mx-auto">
        <div className="border-b border-outline-variant pb-4 flex justify-between items-center">
          <div>
            <h3 className="font-bold text-lg text-on-surface">{order.restaurantName}</h3>
            <p className="text-xs text-secondary">{order.date}</p>
          </div>
          <span className="bg-tertiary-container/10 text-tertiary text-xs font-extrabold px-3 py-1 rounded-full">{order.status.toUpperCase()}</span>
        </div>

        <div className="space-y-3">
          {order.items.map((item) => (
            <div key={item.id} className="flex justify-between items-start text-sm">
              <div>
                <span className="font-semibold text-on-surface">{item.name} x {item.quantity}</span>
                {item.customizations && item.customizations.map((c, i) => (
                  <div key={i} className="text-xs text-secondary">{c}</div>
                ))}
              </div>
              <span className="font-bold text-on-surface">₹{item.price * item.quantity}</span>
            </div>
          ))}
        </div>

        <hr className="border-outline-variant" />

        <div className="space-y-2 text-sm text-secondary font-medium">
          <div className="flex justify-between"><span>Subtotal</span><span>₹{order.subtotal}</span></div>
          <div className="flex justify-between"><span>Delivery Fee</span><span>₹{order.deliveryFee}</span></div>
          <div className="flex justify-between"><span>GST & Tax</span><span>₹{order.tax}</span></div>
          {order.discount > 0 && <div className="flex justify-between text-tertiary"><span>Discount Applied</span><span>-₹{order.discount}</span></div>}
        </div>

        <hr className="border-outline-variant" />

        <div className="flex justify-between font-bold text-lg text-on-surface">
          <span>Amount Paid</span>
          <span>₹{order.total}</span>
        </div>
      </div>
    </div>
  );
};

// 15. WALLET & PAYMENTS
export const WalletPayments: React.FC = () => {
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.auth);
  const [amount, setAmount] = useState("");
  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);

  const fetchWalletData = useCallback(() => {
    apiService.getTransactions().then(setTransactions);
    apiService.getWalletBalance().then((bal) => dispatch(updateWalletBalance(bal)));
  }, [dispatch]);

  useEffect(() => {
    fetchWalletData();
  }, [fetchWalletData]);

  const handleLoadFunds = async () => {
    const val = parseFloat(amount);
    if (isNaN(val) || val <= 0) {
      toast.error("Please enter a valid amount");
      return;
    }
    const newBal = await apiService.addWalletFunds(val);
    dispatch(updateWalletBalance(newBal));
    setAmount("");
    toast.success(`₹${val} loaded successfully!`);
    fetchWalletData();
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Wallet & Payments</h2>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        {/* Balance Card */}
        <div className="bg-primary-container text-white p-6 rounded-2xl space-y-4 shadow-xl flex flex-col justify-between">
          <div>
            <span className="text-xs font-semibold opacity-90 block">CURRENT WALLET BALANCE</span>
            <span className="text-4xl font-extrabold block mt-2">₹{user?.walletBalance ?? 762}</span>
          </div>
          <div className="flex gap-2">
            <input 
              type="number" 
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="Enter amount"
              className="w-full px-3 py-1.5 rounded-lg text-on-surface font-semibold text-sm focus:outline-none"
            />
            <button 
              onClick={handleLoadFunds}
              className="bg-white text-primary-container font-extrabold px-4 py-1.5 rounded-lg text-sm shrink-0 hover:scale-105 transition-transform"
            >
              Load Funds
            </button>
          </div>
        </div>

        {/* Transaction History */}
        <div className="lg:col-span-2 bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
          <h3 className="font-bold text-lg text-on-surface">Transaction Logs</h3>
          <div className="divide-y divide-outline-variant">
            {transactions.map((tx) => (
              <div key={tx.id} className="py-3 flex justify-between items-center">
                <div>
                  <span className="font-semibold text-sm text-on-surface block">{tx.description}</span>
                  <span className="text-xs text-secondary">{tx.date}</span>
                </div>
                <span className={`font-bold ${tx.type === "credit" ? "text-tertiary" : "text-red-600"}`}>
                  {tx.type === "credit" ? "+" : "-"}₹{tx.amount}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

// 16. ACCOUNT SETTINGS
export const AccountSettings: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Account Settings</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
        <h3 className="font-bold text-lg text-on-surface">System Configurations</h3>
        <p className="text-secondary text-sm">Review your communication, layout theme, and accessibility settings.</p>
        <div className="space-y-2">
          <label className="flex items-center gap-3 p-3 border border-outline-variant rounded-xl cursor-pointer">
            <input type="checkbox" defaultChecked className="text-primary-container rounded" />
            <span className="text-sm font-semibold text-on-surface">Receive order progress SMS notifications</span>
          </label>
          <label className="flex items-center gap-3 p-3 border border-outline-variant rounded-xl cursor-pointer">
            <input type="checkbox" defaultChecked className="text-primary-container rounded" />
            <span className="text-sm font-semibold text-on-surface">Enable wallet auto-debit on checkout</span>
          </label>
        </div>
      </div>
    </div>
  );
};

// 17. EDIT PROFILE
export const EditProfile: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const [name, setName] = useState(user?.name || "");

  const handleSave = () => {
    toast.success("Profile saved!");
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Edit Profile</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 max-w-md">
        <div>
          <label className="block text-sm font-semibold mb-1">Full Name</label>
          <input 
            type="text" 
            value={name} 
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-2 border border-outline-variant rounded-xl"
          />
        </div>
        <button onClick={handleSave} className="bg-primary-container text-white px-6 py-2 rounded-full font-bold">Save Changes</button>
      </div>
    </div>
  );
};

// 18. SAVED & WISHLIST
export const SavedWishlist: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Saved & Wishlist</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary">
        <span className="material-symbols-outlined text-4xl block mb-2">favorite</span>
        No bookmarks saved yet.
      </div>
    </div>
  );
};

// 19. HELP CENTER
export const HelpCenter: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Help Center</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
        <h3 className="font-bold text-lg text-on-surface">Frequently Asked Questions</h3>
        <div className="space-y-3 text-sm text-secondary">
          <div>
            <h4 className="font-bold text-on-surface mb-1">How can I request a refund?</h4>
            <p>Go to your Order Details screen and click "Raise Dispute" or email support@quickbite.com.</p>
          </div>
          <div>
            <h4 className="font-bold text-on-surface mb-1">How long does delivery take?</h4>
            <p>Average delivery takes 25 to 40 minutes depending on distance and restaurant cooking schedules.</p>
          </div>
        </div>
      </div>
    </div>
  );
};
