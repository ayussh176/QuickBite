import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { apiService } from "../services/api/apiClient";
import type { MenuItem, Order } from "../services/api/mockData";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import toast from "react-hot-toast";

const mockRevenueData = [
  { day: "Mon", revenue: 4200 },
  { day: "Tue", revenue: 3800 },
  { day: "Wed", revenue: 5100 },
  { day: "Thu", revenue: 4600 },
  { day: "Fri", revenue: 7800 },
  { day: "Sat", revenue: 9200 },
  { day: "Sun", revenue: 8500 }
];

// 1. MERCHANT DASHBOARD
export const MerchantDashboard: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    apiService.getOrders().then(setOrders);
  }, []);

  const stats = [
    { label: "Today's Revenue", value: "₹18,450", icon: "payments", change: "+12.4% from yesterday" },
    { label: "Total Orders", value: orders.length.toString(), icon: "receipt_long", change: "2 active now" },
    { label: "Avg Prep Time", value: "18 mins", icon: "timer", change: "-2 mins from avg" },
    { label: "Avg Rating", value: "4.6 ★", icon: "star", change: "Based on 5K+ reviews" }
  ];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-headline-md font-bold text-on-surface">Overview</h2>
          <p className="text-secondary text-sm">Real-time metrics for The Burger House</p>
        </div>
      </div>

      {/* Stats Bento Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {stats.map((stat, i) => (
          <div key={i} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-2 relative overflow-hidden shadow-sm">
            <span className="material-symbols-outlined absolute right-4 top-4 text-primary-container bg-primary-container/10 p-2 rounded-xl text-2xl">
              {stat.icon}
            </span>
            <span className="text-xs font-semibold text-secondary block">{stat.label}</span>
            <span className="text-3xl font-extrabold text-on-surface block">{stat.value}</span>
            <span className="text-xs text-tertiary font-bold block">{stat.change}</span>
          </div>
        ))}
      </div>

      {/* Analytics Graph & Orders Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        <div className="lg:col-span-2 bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
          <h3 className="font-bold text-lg text-on-surface">Weekly Revenue Analysis</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockRevenueData}>
                <defs>
                  <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#ff6b00" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#ff6b00" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Area type="monotone" dataKey="revenue" stroke="#ff6b00" strokeWidth={3} fillOpacity={1} fill="url(#colorRevenue)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Active Orders Quick Queue */}
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 h-fit">
          <h3 className="font-bold text-lg text-on-surface">Order Queue</h3>
          <div className="divide-y divide-outline-variant">
            {orders.map((o) => (
              <div key={o.id} className="py-3 flex justify-between items-center">
                <div>
                  <span className="font-bold text-sm text-on-surface block">#{o.id}</span>
                  <span className="text-xs text-secondary">{o.items.length} items &bull; ₹{o.total}</span>
                </div>
                <span className="bg-primary-container/10 text-primary-container text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase">
                  {o.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

// 2. MENU MANAGEMENT
export const MerchantMenu: React.FC = () => {
  const navigate = useNavigate();
  const [menu, setMenu] = useState<MenuItem[]>([]);

  useEffect(() => {
    apiService.getRestaurantById("a8bd6caddd574bae9da4e934e5887ede").then((r) => {
      if (r) setMenu(r.menu);
    });
  }, []);

  const handleDelete = (id: string) => {
    setMenu(menu.filter((m) => m.id !== id));
    toast.success("Food item removed from menu");
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-headline-md font-bold text-on-surface">Menu Management</h2>
          <p className="text-secondary text-sm">List, update, and manage your dishes</p>
        </div>
        <button 
          onClick={() => navigate("/merchant/menu/edit/new")}
          className="bg-primary-container text-white px-6 py-2.5 rounded-full font-button font-bold hover:bg-primary transition-all flex items-center gap-2"
        >
          <span className="material-symbols-outlined text-sm">add</span>
          Add Food Item
        </button>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
              <th className="p-4">Dish</th>
              <th className="p-4">Category</th>
              <th className="p-4">Price</th>
              <th className="p-4">Type</th>
              <th className="p-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {menu.map((item) => (
              <tr key={item.id} className="hover:bg-surface-container-low/50">
                <td className="p-4 font-bold text-on-surface">{item.name}</td>
                <td className="p-4 text-secondary">{item.category}</td>
                <td className="p-4 font-semibold text-on-surface">₹{item.price}</td>
                <td className="p-4">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${item.isVeg ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                    {item.isVeg ? "VEG" : "NON-VEG"}
                  </span>
                </td>
                <td className="p-4 text-center flex justify-center gap-2">
                  <button 
                    onClick={() => navigate(`/merchant/menu/edit/${item.id}`)}
                    className="p-1 text-secondary hover:text-primary-container"
                  >
                    <span className="material-symbols-outlined text-lg">edit</span>
                  </button>
                  <button 
                    onClick={() => handleDelete(item.id)}
                    className="p-1 text-secondary hover:text-red-600"
                  >
                    <span className="material-symbols-outlined text-lg">delete</span>
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const menuItemSchema = z.object({
  name: z.string().min(1, "Dish Name is required").min(3, "Dish Name must be at least 3 characters"),
  price: z.number({ message: "Price must be a number" }).min(1, "Price must be greater than 0"),
  category: z.string().min(1, "Category is required"),
  isVeg: z.boolean()
});

type MenuItemFormValues = z.infer<typeof menuItemSchema>;

// 3. EDIT FOOD ITEM
export const MerchantEditItem: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting }
  } = useForm<MenuItemFormValues>({
    resolver: zodResolver(menuItemSchema),
    defaultValues: {
      name: "",
      price: 0,
      category: "Burgers",
      isVeg: true
    }
  });

  const isVeg = watch("isVeg");

  useEffect(() => {
    if (id && id !== "new") {
      apiService.getRestaurantById("a8bd6caddd574bae9da4e934e5887ede").then((r) => {
        const dish = r?.menu.find(m => m.id === id);
        if (dish) {
          reset({
            name: dish.name,
            price: dish.price,
            category: dish.category,
            isVeg: dish.isVeg
          });
        }
      });
    }
  }, [id, reset]);

  const onSubmit = async (data: MenuItemFormValues) => {
    console.log("Saving item:", data);
    await new Promise((resolve) => setTimeout(resolve, 300));
    toast.success("Menu item saved successfully!");
    navigate("/merchant/menu");
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">{id === "new" ? "Add" : "Edit"} Food Item</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl max-w-xl shadow-sm">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1">Dish Name</label>
            <input 
              type="text" 
              placeholder="e.g. Garlic Butter Mushroom Burger"
              {...register("name")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.name ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.name && (
              <span className="text-red-500 text-xs mt-1 block">{errors.name.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1">Price (₹)</label>
            <input 
              type="number" 
              placeholder="199"
              {...register("price", { valueAsNumber: true })}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.price ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.price && (
              <span className="text-red-500 text-xs mt-1 block">{errors.price.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1">Category</label>
            <select 
              {...register("category")}
              className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-white focus:outline-none focus:border-primary-container"
            >
              <option value="Burgers">Burgers</option>
              <option value="Sides">Sides</option>
              <option value="Beverages">Beverages</option>
              <option value="Pizza">Pizza</option>
            </select>
            {errors.category && (
              <span className="text-red-500 text-xs mt-1 block">{errors.category.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1">Food Preference</label>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 cursor-pointer text-sm font-medium">
                <input 
                  type="radio" 
                  name="isVeg" 
                  checked={isVeg === true}
                  onChange={() => setValue("isVeg", true)}
                  className="text-primary-container focus:ring-primary-container h-4 w-4"
                />
                Pure Vegetarian (Veg)
              </label>
              <label className="flex items-center gap-2 cursor-pointer text-sm font-medium">
                <input 
                  type="radio" 
                  name="isVeg" 
                  checked={isVeg === false}
                  onChange={() => setValue("isVeg", false)}
                  className="text-primary-container focus:ring-primary-container h-4 w-4"
                />
                Non-Vegetarian
              </label>
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button 
              type="submit" 
              disabled={isSubmitting}
              className="bg-primary-container text-white px-6 py-2 rounded-full font-bold hover:bg-primary transition-all disabled:opacity-50"
            >
              {isSubmitting ? "Saving..." : "Save Item"}
            </button>
            <button 
              type="button" 
              onClick={() => navigate("/merchant/menu")} 
              className="bg-white border border-outline-variant px-6 py-2 rounded-full font-bold hover:bg-surface-container-low transition-all"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// 4. ORDER MANAGEMENT
export const MerchantOrders: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    apiService.getOrders().then(setOrders);
  }, []);

  const handleStatusChange = (id: string, newStatus: Order["status"]) => {
    setOrders(orders.map((o) => o.id === id ? { ...o, status: newStatus } : o));
    toast.success(`Order #${id} marked as ${newStatus}`);
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Order Management</h2>
      <div className="space-y-4">
        {orders.map((o) => (
          <div key={o.id} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 shadow-sm">
            <div className="flex justify-between items-center border-b border-outline-variant pb-3">
              <div>
                <span className="font-bold text-lg text-on-surface block">Order #{o.id}</span>
                <span className="text-xs text-secondary">{o.date} &bull; {o.address}</span>
              </div>
              <span className="bg-primary-container/10 text-primary-container text-xs font-extrabold px-3 py-1 rounded-full uppercase">{o.status}</span>
            </div>

            <div className="space-y-2">
              {o.items.map((i, idx) => (
                <div key={idx} className="flex justify-between text-sm">
                  <span>{i.name} x {i.quantity}</span>
                  <span className="font-bold">₹{i.price * i.quantity}</span>
                </div>
              ))}
            </div>

            {o.status === "preparing" && (
              <div className="flex gap-2 justify-end pt-3">
                <button 
                  onClick={() => handleStatusChange(o.id, "picked_up")}
                  className="bg-primary-container text-white px-5 py-1.5 rounded-full font-bold text-xs hover:bg-primary transition-all"
                >
                  Mark Ready & Dispatched
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

// 5. REVENUE ANALYTICS
export const MerchantRevenue: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Revenue Analytics</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary">
        <span className="material-symbols-outlined text-4xl block mb-2">insights</span>
        Interactive revenue analysis dashboard is live. Track weekly, monthly, and aggregate payouts.
      </div>
    </div>
  );
};

// 6. RESTAURANT PROFILE
export const MerchantProfile: React.FC = () => {
  const [address, setAddress] = useState("Plot 42, Sector 18, Commercial Hub, Noida");

  const handleSave = () => {
    toast.success("Merchant Profile updated!");
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Restaurant Profile</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 max-w-xl">
        <div>
          <label className="block text-sm font-semibold mb-1">Restaurant Name</label>
          <input type="text" disabled value="The Burger House" className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-neutral-50 text-secondary" />
        </div>
        <div>
          <label className="block text-sm font-semibold mb-1">Outlet Address</label>
          <input type="text" value={address} onChange={(e) => setAddress(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl" />
        </div>
        <button onClick={handleSave} className="bg-primary-container text-white px-6 py-2 rounded-full font-bold">Update Details</button>
      </div>
    </div>
  );
};

// 7. STAFF MANAGEMENT
export const MerchantStaff: React.FC = () => {
  const [staff] = useState([
    { name: "Vikram Sethi", role: "Head Chef" },
    { name: "Rohit Nair", role: "Kitchen Assistant" }
  ]);

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Staff Management</h2>
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
            <tr>
              <th className="p-4">Name</th>
              <th className="p-4">Role</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {staff.map((s, idx) => (
              <tr key={idx}>
                <td className="p-4 font-bold text-on-surface">{s.name}</td>
                <td className="p-4 text-secondary">{s.role}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// 8. OFFERS & COUPONS
export const MerchantOffers: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Offers & Coupons</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary">
        No active custom store coupons. Global platform coupons (WELCOME50, QUICKBITE100) are active.
      </div>
    </div>
  );
};

// 9. CUSTOMER REVIEWS
export const MerchantReviews: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Customer Reviews</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary">
        Review feedback loops are active. Partner reviews aggregated score is 4.6★.
      </div>
    </div>
  );
};
