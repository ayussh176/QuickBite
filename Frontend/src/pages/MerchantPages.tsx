import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { apiService, api } from "../services/api/apiClient";
import type { MenuItem, Order, Coupon } from "../services/api/apiClient";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import toast from "react-hot-toast";

// 1. MERCHANT DASHBOARD
export const MerchantDashboard: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [restaurant, setRestaurant] = useState<any>(null);
  const [analytics, setAnalytics] = useState<any>(null);

  useEffect(() => {
    apiService.getOrders().then(setOrders).catch(console.error);
    apiService.getMyRestaurant().then(setRestaurant).catch(console.error);
    apiService.getMerchantAnalytics().then(setAnalytics).catch(console.error);
  }, []);

  const totalOrders = orders.length;
  const activeOrdersCount = orders.filter(o => o.status !== "delivered" && o.status !== "cancelled").length;
  const totalRevenue = orders
    .filter(o => o.status === "delivered" || o.status === "picked_up")
    .reduce((sum, o) => sum + o.total, 0);

  // Group orders by day of week for the chart
  const daysOfWeek = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const revenueByDayMap: Record<string, number> = { Sun: 0, Mon: 0, Tue: 0, Wed: 0, Thu: 0, Fri: 0, Sat: 0 };
  
  orders.forEach(o => {
    if (o.status === "delivered" || o.status === "picked_up") {
      try {
        const d = new Date(o.date);
        const dayName = daysOfWeek[d.getDay()];
        revenueByDayMap[dayName] = (revenueByDayMap[dayName] || 0) + o.total;
      } catch (e) {}
    }
  });

  const chartData = daysOfWeek.map(day => ({
    day,
    revenue: revenueByDayMap[day] || 0
  }));

  const stats = [
    { label: "Today's Revenue", value: `₹${totalRevenue}`, icon: "payments", change: "Updated in real-time" },
    { label: "Total Orders", value: (analytics?.totalOrders ?? totalOrders).toString(), icon: "receipt_long", change: `${analytics?.activeOrders ?? activeOrdersCount} active now` },
    { label: "Menu Items", value: (analytics?.availableMenuItems ?? 0).toString(), icon: "restaurant_menu", change: `${analytics?.lowStockItems ?? 0} low-stock alerts` },
    { label: "Avg Rating", value: restaurant ? `${restaurant.rating} ★` : "4.6 ★", icon: "star", change: "Customer feedback score" }
  ];

  return (
    <div className="space-y-6">
      {restaurant && !restaurant.isOpen && (
        <div className="bg-amber-50 border-l-4 border-amber-500 p-4 rounded-r-xl text-amber-800 text-sm shadow-sm">
          <div className="flex items-center gap-2 font-bold">
            <span className="material-symbols-outlined">warning</span>
            Restaurant Verification: PENDING APPROVAL
          </div>
          Your outlet application is currently being reviewed by Admin. Customer orders will become active once approved.
        </div>
      )}

      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-headline-md font-bold text-on-surface">Overview</h2>
          <p className="text-secondary text-sm">Real-time metrics for {restaurant?.name || "The Burger House"}</p>
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
              <AreaChart data={chartData}>
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
          <div className="divide-y divide-outline-variant max-h-[300px] overflow-y-auto">
            {orders.filter(o => o.status !== "delivered" && o.status !== "cancelled").map((o) => (
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
            {orders.filter(o => o.status !== "delivered" && o.status !== "cancelled").length === 0 && (
              <p className="text-secondary text-sm py-4">No active orders in queue.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// 2. MENU MANAGEMENT
export const MerchantMenu: React.FC = () => {
  const navigate = useNavigate();
  const [restaurantId, setRestaurantId] = useState<string>("");
  const [menu, setMenu] = useState<MenuItem[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [newCategory, setNewCategory] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchMenu = async () => {
    setLoading(true);
    try {
      const rest = await apiService.getMyRestaurant();
      if (rest) {
        setRestaurantId(rest.id);
        const [categoryList, menuPage] = await Promise.all([
          apiService.getCategories(rest.id),
          apiService.getMenuPage(rest.id, {
            search,
            page,
            size: 5,
            categoryId: selectedCategory
          })
        ]);

        setCategories(categoryList);
        setMenu(menuPage.content);
        setTotalPages(menuPage.totalPages || 1);
        setTotalItems(menuPage.totalElements || 0);
      }
    } catch (e) {
      console.error("Error loading menu", e);
      toast.error("Unable to load menu catalog");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMenu();
  }, [search, page, selectedCategory]);

  const handleDelete = async (id: string) => {
    if (!window.confirm("Are you sure you want to delete this food item?")) return;
    try {
      await apiService.deleteMenuItem(restaurantId, id);
      toast.success("Food item removed from menu");
      fetchMenu();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete food item");
    }
  };

  const handleCreateCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!restaurantId || !newCategory.trim()) return;
    try {
      await apiService.createCategory(restaurantId, newCategory.trim());
      toast.success("Category saved");
      setNewCategory("");
      fetchMenu();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to save category");
    }
  };

  const handleDeleteCategory = async (categoryId: string) => {
    if (!window.confirm("Delete this category and hide its menu items?")) return;
    try {
      await apiService.deleteCategory(restaurantId, categoryId);
      if (selectedCategory === categoryId) {
        setSelectedCategory("");
        setPage(0);
      }
      toast.success("Category removed");
      fetchMenu();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete category");
    }
  };

  const categoryNameById = (categoryId?: string) => {
    const category = categories.find((c) => String(c.id) === String(categoryId));
    return category?.name || `ID: ${categoryId || "General"}`;
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
          className="bg-primary text-white font-bold px-6 py-2.5 rounded-full hover:bg-primary-container transition-all text-sm flex items-center gap-2 shadow-sm"
        >
          <span className="material-symbols-outlined text-sm">add</span> Add Dish
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-4 items-start">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1 max-w-md">
            <span className="material-symbols-outlined absolute left-3 top-2.5 text-secondary">search</span>
            <input
              type="text"
              placeholder="Search dishes..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full pl-10 pr-4 py-2 border border-outline-variant rounded-full focus:outline-none focus:border-primary-container text-sm"
            />
          </div>
          <select
            value={selectedCategory}
            onChange={(e) => {
              setSelectedCategory(e.target.value);
              setPage(0);
            }}
            className="px-4 py-2 border border-outline-variant rounded-full bg-white text-sm focus:outline-none focus:border-primary-container"
          >
            <option value="">All Categories</option>
            {categories.map((category) => (
              <option key={category.id} value={String(category.id)}>{category.name}</option>
            ))}
          </select>
        </div>

        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-4 space-y-3 shadow-sm">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-sm text-on-surface">Categories</h3>
            <span className="text-xs text-secondary">{categories.length} active</span>
          </div>
          <form onSubmit={handleCreateCategory} className="flex gap-2">
            <input
              value={newCategory}
              onChange={(e) => setNewCategory(e.target.value)}
              placeholder="New category"
              className="min-w-0 flex-1 px-3 py-2 border border-outline-variant rounded-xl text-xs"
            />
            <button type="submit" className="bg-primary-container text-white px-3 py-2 rounded-xl text-xs font-bold">
              Add
            </button>
          </form>
          <div className="space-y-1 max-h-40 overflow-y-auto">
            {categories.map((category) => (
              <div key={category.id} className="flex items-center justify-between gap-2 text-xs bg-surface-container p-2 rounded-lg">
                <span className="font-semibold truncate">{category.name}</span>
                <button
                  type="button"
                  onClick={() => handleDeleteCategory(String(category.id))}
                  className="text-red-500 font-bold"
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-12 text-secondary">Loading menu catalog...</div>
      ) : (
        <div className="space-y-4">
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
            <table className="w-full text-left">
              <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
                <tr>
                  <th className="p-4">Dish</th>
                  <th className="p-4">Category</th>
                  <th className="p-4">Price</th>
                  <th className="p-4 text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant text-sm">
                {menu.map((dish) => (
                  <tr key={dish.id} className="hover:bg-neutral-50/50">
                    <td className="p-4 flex items-center gap-3">
                      <img src={dish.image} alt={dish.name} className="w-12 h-12 rounded-xl object-cover" />
                      <div>
                        <span className="font-bold text-on-surface flex items-center gap-1.5">
                          {dish.name}
                          <span className={`w-2.5 h-2.5 rounded-full ${dish.isVeg ? "bg-green-500" : "bg-red-500"}`} title={dish.isVeg ? "Veg" : "Non-Veg"}></span>
                        </span>
                        <span className="text-xs text-secondary line-clamp-1">{dish.description}</span>
                      </div>
                    </td>
                    <td className="p-4 text-secondary">{categoryNameById(dish.categoryId || dish.category)}</td>
                    <td className="p-4 font-bold">₹{dish.price}</td>
                    <td className="p-4 text-center flex justify-center gap-2 pt-6">
                      <button 
                        onClick={() => navigate(`/merchant/menu/edit/${dish.id}`)}
                        className="text-primary hover:text-primary-container font-semibold"
                      >
                        Edit
                      </button>
                      <span className="text-outline-variant">|</span>
                      <button 
                        onClick={() => handleDelete(dish.id)}
                        className="text-red-500 hover:text-red-700 font-semibold"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {menu.length === 0 && (
                  <tr>
                    <td colSpan={4} className="p-8 text-center text-secondary">No dishes found in menu catalog.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex justify-between items-center pt-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="bg-white border border-outline-variant px-4 py-1.5 rounded-full font-semibold text-xs hover:bg-neutral-50 disabled:opacity-50"
              >
                Previous
              </button>
              <span className="text-xs text-secondary">Page {page + 1} of {totalPages} &bull; {totalItems} dishes</span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="bg-white border border-outline-variant px-4 py-1.5 rounded-full font-semibold text-xs hover:bg-neutral-50 disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

const menuItemSchema = z.object({
  name: z.string().min(1, "Dish Name is required").min(3, "Dish Name must be at least 3 characters"),
  description: z.string().optional(),
  price: z.number({ message: "Price must be a number" }).min(1, "Price must be greater than 0"),
  categoryId: z.string().min(1, "Category is required"),
  imageUrl: z.string().optional(),
  preparationTime: z.number({ message: "Preparation time must be a number" }).min(1, "Preparation time is required"),
  isVeg: z.boolean(),
  available: z.boolean(),
  bestseller: z.boolean()
});

type MenuItemFormValues = z.infer<typeof menuItemSchema>;

// 3. EDIT FOOD ITEM
export const MerchantEditItem: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [restaurantId, setRestaurantId] = useState("");
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

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
      description: "",
      price: 0,
      categoryId: "",
      imageUrl: "",
      preparationTime: 15,
      isVeg: true,
      available: true,
      bestseller: false
    }
  });

  const isVeg = watch("isVeg");
  const imageUrl = watch("imageUrl");
  const available = watch("available");
  const bestseller = watch("bestseller");

  useEffect(() => {
    const loadItemForm = async () => {
      setLoading(true);
      try {
        const r = await apiService.getMyRestaurant();
        if (!r) return;

        setRestaurantId(r.id);
        let categoryList = await apiService.getCategories(r.id);
        if (categoryList.length === 0) {
          const general = await apiService.createCategory(r.id, "General");
          categoryList = [general];
        }

        setCategories(categoryList);
        const defaultCategoryId = String(categoryList[0]?.id || "");

        if (id && id !== "new") {
          const dish = await apiService.getMenuItem(r.id, id);
          const primaryImage = dish.images?.find((img: any) => img.primary) || dish.images?.[0];
          reset({
            name: dish.name || "",
            description: dish.description || "",
            price: Number(dish.price || 0),
            categoryId: String(dish.categoryId || defaultCategoryId),
            imageUrl: primaryImage?.imageUrl || "",
            preparationTime: Number(dish.preparationTime || 15),
            isVeg: dish.foodType === "VEG",
            available: dish.available ?? true,
            bestseller: dish.bestseller ?? false
          });
        } else {
          reset({
            name: "",
            description: "",
            price: 0,
            categoryId: defaultCategoryId,
            imageUrl: "",
            preparationTime: 15,
            isVeg: true,
            available: true,
            bestseller: false
          });
        }
      } catch (e: any) {
        toast.error(e.response?.data?.message || "Failed to load dish form");
      } finally {
        setLoading(false);
      }
    };

    loadItemForm();
  }, [id, reset]);

  const handleImageUpload = async (file?: File) => {
    if (!file) return;
    setUploading(true);
    try {
      const upload = await apiService.uploadImage(file);
      setValue("imageUrl", upload.imageUrl, { shouldDirty: true, shouldValidate: true });
      toast.success("Image uploaded");
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Image upload failed");
    } finally {
      setUploading(false);
    }
  };

  const onSubmit = async (data: MenuItemFormValues) => {
    try {
      const payload = {
        name: data.name,
        description: data.description?.trim() || `${data.name} - freshly prepared and delicious.`,
        price: data.price,
        discountedPrice: data.price,
        foodType: data.isVeg ? "VEG" : "NON_VEG",
        categoryId: Number(data.categoryId),
        available: data.available,
        bestseller: data.bestseller,
        preparationTime: data.preparationTime,
        images: data.imageUrl?.trim() ? [{ imageUrl: data.imageUrl.trim(), primary: true, sortOrder: 0 }] : [],
        variants: [],
        addOns: []
      };

      if (id === "new") {
        await apiService.createMenuItem(restaurantId, payload);
        toast.success("Dish created successfully!");
      } else {
        await apiService.updateMenuItem(restaurantId, id!, payload);
        toast.success("Dish details updated successfully!");
      }
      navigate("/merchant/menu");
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to save dish details");
    }
  };

  if (loading) return <div className="text-center py-12 text-secondary">Loading dish editor...</div>;

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
            <label className="block text-sm font-semibold mb-1">Description</label>
            <textarea
              placeholder="Short description shown to customers"
              {...register("description")}
              className="w-full px-4 py-2 border border-outline-variant rounded-xl focus:outline-none focus:border-primary-container h-24 text-sm"
            />
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
            <label className="block text-sm font-semibold mb-1">Prep Time (mins)</label>
            <input
              type="number"
              placeholder="15"
              {...register("preparationTime", { valueAsNumber: true })}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.preparationTime ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.preparationTime && (
              <span className="text-red-500 text-xs mt-1 block">{errors.preparationTime.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1">Category</label>
            <select 
              {...register("categoryId")}
              className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-white focus:outline-none focus:border-primary-container"
            >
              {categories.map((category) => (
                <option key={category.id} value={String(category.id)}>{category.name}</option>
              ))}
            </select>
            {errors.categoryId && (
              <span className="text-red-500 text-xs mt-1 block">{errors.categoryId.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1">Dish Image</label>
            <div className="flex gap-3 items-start">
              <div className="w-20 h-20 rounded-xl overflow-hidden border border-outline-variant bg-surface-container shrink-0">
                {imageUrl ? (
                  <img src={imageUrl} alt="Dish preview" className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-secondary">
                    <span className="material-symbols-outlined">image</span>
                  </div>
                )}
              </div>
              <div className="flex-1 space-y-2">
                <input
                  type="url"
                  placeholder="https://..."
                  {...register("imageUrl")}
                  className="w-full px-4 py-2 border border-outline-variant rounded-xl focus:outline-none focus:border-primary-container text-sm"
                />
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/webp"
                  disabled={uploading}
                  onChange={(e) => handleImageUpload(e.target.files?.[0])}
                  className="block w-full text-xs text-secondary file:mr-3 file:border-0 file:bg-primary-container file:text-white file:px-3 file:py-2 file:rounded-full file:font-bold"
                />
              </div>
            </div>
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
                Vegetarian
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

          <div className="flex flex-wrap gap-4">
            <label className="flex items-center gap-2 cursor-pointer text-sm font-medium">
              <input
                type="checkbox"
                checked={available}
                onChange={(e) => setValue("available", e.target.checked)}
                className="text-primary-container focus:ring-primary-container h-4 w-4"
              />
              Available for ordering
            </label>
            <label className="flex items-center gap-2 cursor-pointer text-sm font-medium">
              <input
                type="checkbox"
                checked={bestseller}
                onChange={(e) => setValue("bestseller", e.target.checked)}
                className="text-primary-container focus:ring-primary-container h-4 w-4"
              />
              Mark as bestseller
            </label>
          </div>

          <div className="flex gap-3 pt-4">
            <button 
              type="submit" 
              disabled={isSubmitting || uploading}
              className="bg-primary-container text-white px-6 py-2 rounded-full font-bold hover:bg-primary transition-all disabled:opacity-50 text-sm"
            >
              {isSubmitting ? "Saving..." : uploading ? "Uploading..." : "Save Item"}
            </button>
            <button 
              type="button" 
              onClick={() => navigate("/merchant/menu")} 
              className="bg-white border border-outline-variant px-6 py-2 rounded-full font-bold hover:bg-surface-container-low transition-all text-sm"
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
  const [loading, setLoading] = useState(true);

  const fetchOrders = () => {
    apiService.getMerchantOrderQueue()
      .then(setOrders)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  useEffect(() => {
    const handleOrderUpdate = () => {
      console.log("Real-time order update received at Merchant. Refreshing queue...");
      apiService.getMerchantOrderQueue().then(setOrders).catch(console.error);
    };

    window.addEventListener("orderUpdate", handleOrderUpdate);
    return () => {
      window.removeEventListener("orderUpdate", handleOrderUpdate);
    };
  }, []);

  const handleStatusChange = async (id: string, newStatus: Order["status"]) => {
    try {
      await apiService.updateOrderStatus(id, newStatus);
      toast.success(`Order #${id} status updated to ${newStatus}`);
      
      const updated = await apiService.getMerchantOrderQueue();
      setOrders(updated);
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update order status");
    }
  };

  const nextMerchantStatus = (status: Order["status"]): Order["status"] | null => {
    if (status === "pending") return "confirmed";
    if (status === "confirmed") return "preparing";
    if (status === "preparing") return "ready_for_pickup";
    return null;
  };

  const nextMerchantLabel = (status: Order["status"]) => {
    if (status === "pending") return "Accept Order";
    if (status === "confirmed") return "Start Preparing";
    if (status === "preparing") return "Mark Ready for Pickup";
    return "";
  };

  if (loading) return <div className="text-center py-12 text-secondary">Loading order queue...</div>;

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

            {nextMerchantStatus(o.status) && (
              <div className="flex gap-2 justify-end pt-3">
                <button 
                  onClick={() => handleStatusChange(o.id, nextMerchantStatus(o.status)!)}
                  className="bg-primary-container text-white px-5 py-1.5 rounded-full font-bold text-xs hover:bg-primary transition-all"
                >
                  {nextMerchantLabel(o.status)}
                </button>
              </div>
            )}
          </div>
        ))}
        {orders.length === 0 && (
          <p className="text-secondary text-sm">No orders recorded for this outlet.</p>
        )}
      </div>
    </div>
  );
};

// 5. REVENUE ANALYTICS
export const MerchantRevenue: React.FC = () => {
  const [revenue, setRevenue] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiService.getMerchantRevenue()
      .then(setRevenue)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const dailyRevenue = revenue?.dailyRevenue || [];

  if (loading) return <div className="text-center py-12 text-secondary">Loading revenue logs...</div>;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Revenue Analytics</h2>
        <p className="text-secondary text-sm">Review your aggregate platform payouts and settlements</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Gross Revenue</span>
          <span className="text-3xl font-extrabold text-primary-container">₹{Number(revenue?.grossRevenue || 0)}</span>
        </div>
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Completed Deliveries</span>
          <span className="text-3xl font-extrabold text-on-surface">{revenue?.completedOrders || 0}</span>
        </div>
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Platform Fee Deductions</span>
          <span className="text-3xl font-extrabold text-secondary">₹{Number(revenue?.platformFees || 0)} (5%)</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Net Revenue</span>
          <span className="text-2xl font-extrabold text-on-surface">₹{Number(revenue?.netRevenue || 0)}</span>
        </div>
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Average Order Value</span>
          <span className="text-2xl font-extrabold text-on-surface">₹{Number(revenue?.averageOrderValue || 0)}</span>
        </div>
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm">
          <span className="text-xs text-secondary font-bold block mb-1">Active Orders</span>
          <span className="text-2xl font-extrabold text-on-surface">{revenue?.activeOrders || 0}</span>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm animate-fade-in">
        <table className="w-full text-left">
          <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
            <tr>
              <th className="p-4">Date</th>
              <th className="p-4">Subtotal</th>
              <th className="p-4">Delivery Fee</th>
              <th className="p-4">Tax</th>
              <th className="p-4">Discount</th>
              <th className="p-4">Gross Revenue</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {dailyRevenue.map((row: any) => (
              <tr key={row.date}>
                <td className="p-4 text-secondary">{row.date}</td>
                <td className="p-4">₹{Number(row.subtotal || 0)}</td>
                <td className="p-4">₹{Number(row.deliveryFee || 0)}</td>
                <td className="p-4">₹{Number(row.taxAmount || 0)}</td>
                <td className="p-4">₹{Number(row.discount || 0)}</td>
                <td className="p-4 font-bold text-primary-container">₹{Number(row.totalRevenue || row.totalAmount || 0)}</td>
              </tr>
            ))}
            {dailyRevenue.length === 0 && (
              <tr>
                <td colSpan={6} className="p-8 text-center text-secondary">No revenue payouts recorded yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// 6. RESTAURANT PROFILE
export const MerchantProfile: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"details" | "address" | "upi" | "business">("details");
  const [, setRestaurant] = useState<any>(null);
  const [upiConfigs, setUpiConfigs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Form Fields
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [cuisineType, setCuisineType] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [minOrderAmount, setMinOrderAmount] = useState(0);
  const [deliveryFee, setDeliveryFee] = useState(0);
  const [estimatedDeliveryTime, setEstimatedDeliveryTime] = useState(30);
  const [profileImageUrl, setProfileImageUrl] = useState("");
  const [coverImageUrl, setCoverImageUrl] = useState("");
  const [profileUploading, setProfileUploading] = useState<"profile" | "cover" | null>(null);

  // Address Fields
  const [addressLine1, setAddressLine1] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [zipCode, setZipCode] = useState("");

  // Business Fields
  const [fssaiLicense, setFssaiLicense] = useState("");
  const [gstNumber, setGstNumber] = useState("");

  // New UPI Fields
  const [newUpiId, setNewUpiId] = useState("");
  const [newProvider, setNewProvider] = useState("GPay");

  const loadProfile = async () => {
    setLoading(true);
    try {
      const rest = await apiService.getMyRestaurant();
      if (rest) {
        setRestaurant(rest);
        setName(rest.name);
        setDescription(rest.description || "");
        setCuisineType(rest.cuisine.join(", "));
        setPhone(rest.phone || "");
        setEmail(rest.email || "");
        
        const rawRes = await api.get("/v1/restaurants/my-restaurant");
        const raw = rawRes.data.data;
        setDescription(raw.description || "");
        setMinOrderAmount(Number(raw.minOrderAmount || 0));
        setDeliveryFee(Number(raw.deliveryFee || 0));
        setEstimatedDeliveryTime(raw.estimatedDeliveryTime || 30);
        setFssaiLicense(raw.fssaiLicense || "");
        setGstNumber(raw.gstNumber || "");
        setProfileImageUrl(raw.profileImageUrl || "");
        setCoverImageUrl(raw.coverImageUrl || "");
        
        if (raw.address) {
          setAddressLine1(raw.address.addressLine1 || "");
          setCity(raw.address.city || "");
          setState(raw.address.state || "");
          setZipCode(raw.address.zipCode || "");
        }
      }
      
      const upi = await api.get("/v1/payments/upi-configs");
      setUpiConfigs(upi.data.data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        name,
        description,
        cuisineType,
        phone,
        email,
        fssaiLicense,
        gstNumber,
        openingTime: "10:00:00",
        closingTime: "22:00:00",
        minOrderAmount,
        deliveryFee,
        estimatedDeliveryTime,
        profileImageUrl,
        coverImageUrl,
        address: {
          addressLine1,
          city,
          state,
          zipCode,
          latitude: 28.5708,
          longitude: 77.3258
        }
      };
      
      await apiService.updateMyRestaurant(payload);
      toast.success("Restaurant settings updated!");
      loadProfile();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update settings");
    }
  };

  const handleProfileImageUpload = async (file: File | undefined, target: "profile" | "cover") => {
    if (!file) return;
    setProfileUploading(target);
    try {
      const upload = await apiService.uploadImage(file);
      if (target === "profile") {
        setProfileImageUrl(upload.imageUrl);
      } else {
        setCoverImageUrl(upload.imageUrl);
      }
      toast.success("Image uploaded");
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Image upload failed");
    } finally {
      setProfileUploading(null);
    }
  };

  const handleAddUpi = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUpiId.includes("@")) {
      toast.error("Invalid UPI ID format");
      return;
    }
    try {
      await api.post("/v1/payments/upi-configs", {
        upiId: newUpiId,
        providerName: newProvider,
        isDefault: upiConfigs.length === 0,
        isActive: true
      });
      toast.success("UPI Configuration added!");
      setNewUpiId("");
      loadProfile();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to add UPI config");
    }
  };

  const handleDeleteUpi = async (id: number) => {
    try {
      await api.delete(`/v1/payments/upi-configs/${id}`);
      toast.success("UPI Configuration deleted!");
      loadProfile();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete UPI config");
    }
  };

  if (loading) return <div className="text-center py-12 text-secondary">Loading profile console...</div>;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Restaurant Settings</h2>
        <p className="text-secondary text-sm">Configure outlet branding, delivery parameters, address location, and payment routing</p>
      </div>

      <div className="flex gap-4 border-b border-outline-variant pb-2">
        {(["details", "address", "upi", "business"] as const).map((tab) => (
          <button
            key={tab}
            type="button"
            onClick={() => setActiveTab(tab)}
            className={`font-semibold text-sm pb-2 border-b-2 capitalize transition-all ${
              activeTab === tab ? "border-primary text-primary" : "border-transparent text-secondary hover:text-on-surface"
            }`}
          >
            {tab === "upi" ? "UPI Configuration" : tab === "details" ? "Branding & Delivery" : tab}
          </button>
        ))}
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl shadow-sm max-w-2xl">
        <form onSubmit={handleSaveProfile} className="space-y-4">
          {activeTab === "details" && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1">Restaurant Name</label>
                <input type="text" value={name} onChange={(e) => setName(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
              </div>
              <div>
                <label className="block text-sm font-semibold mb-1">Description</label>
                <textarea value={description} onChange={(e) => setDescription(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl h-24 text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="block text-sm font-semibold">Profile Image</label>
                  <div className="h-28 rounded-xl overflow-hidden border border-outline-variant bg-surface-container">
                    {profileImageUrl ? (
                      <img src={profileImageUrl} alt="Restaurant profile" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-secondary">
                        <span className="material-symbols-outlined">storefront</span>
                      </div>
                    )}
                  </div>
                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/webp"
                    disabled={profileUploading !== null}
                    onChange={(e) => handleProfileImageUpload(e.target.files?.[0], "profile")}
                    className="block w-full text-xs text-secondary file:mr-3 file:border-0 file:bg-primary-container file:text-white file:px-3 file:py-2 file:rounded-full file:font-bold"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-sm font-semibold">Cover Image</label>
                  <div className="h-28 rounded-xl overflow-hidden border border-outline-variant bg-surface-container">
                    {coverImageUrl ? (
                      <img src={coverImageUrl} alt="Restaurant cover" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-secondary">
                        <span className="material-symbols-outlined">panorama</span>
                      </div>
                    )}
                  </div>
                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/webp"
                    disabled={profileUploading !== null}
                    onChange={(e) => handleProfileImageUpload(e.target.files?.[0], "cover")}
                    className="block w-full text-xs text-secondary file:mr-3 file:border-0 file:bg-primary-container file:text-white file:px-3 file:py-2 file:rounded-full file:font-bold"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-1">Cuisine Type(s)</label>
                  <input type="text" value={cuisineType} onChange={(e) => setCuisineType(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" placeholder="e.g. Burgers, Pizza" />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Phone Contact</label>
                  <input type="text" value={phone} onChange={(e) => setPhone(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-1">Min Order (₹)</label>
                  <input type="number" value={minOrderAmount} onChange={(e) => setMinOrderAmount(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Delivery Fee (₹)</label>
                  <input type="number" value={deliveryFee} onChange={(e) => setDeliveryFee(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Prep Time (mins)</label>
                  <input type="number" value={estimatedDeliveryTime} onChange={(e) => setEstimatedDeliveryTime(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
                </div>
              </div>
              <button type="submit" className="bg-primary-container text-white px-6 py-2.5 rounded-full font-bold text-sm">Save Changes</button>
            </div>
          )}

          {activeTab === "address" && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1">Address Line 1</label>
                <input type="text" value={addressLine1} onChange={(e) => setAddressLine1(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-1">City</label>
                  <input type="text" value={city} onChange={(e) => setCity(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">State</label>
                  <input type="text" value={state} onChange={(e) => setState(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Zip Code</label>
                  <input type="text" value={zipCode} onChange={(e) => setZipCode(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
              </div>
              <button type="submit" className="bg-primary-container text-white px-6 py-2.5 rounded-full font-bold text-sm">Save Address</button>
            </div>
          )}

          {activeTab === "business" && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1">FSSAI License Number</label>
                <input type="text" value={fssaiLicense} onChange={(e) => setFssaiLicense(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" placeholder="14-digit FSSAI number" required />
              </div>
              <div>
                <label className="block text-sm font-semibold mb-1">GST Identification Number (GSTIN)</label>
                <input type="text" value={gstNumber} onChange={(e) => setGstNumber(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" placeholder="15-digit GSTIN number" />
              </div>
              <button type="submit" className="bg-primary-container text-white px-6 py-2.5 rounded-full font-bold text-sm">Save Business Details</button>
            </div>
          )}
        </form>

        {activeTab === "upi" && (
          <div className="space-y-6">
            <div className="space-y-3">
              <h3 className="font-bold text-sm text-on-surface">Registered UPI Addresses</h3>
              <div className="space-y-2">
                {upiConfigs.map((config) => (
                  <div key={config.id} className="flex justify-between items-center bg-surface-container border border-outline-variant p-3.5 rounded-xl text-sm animate-fade-in">
                    <div>
                      <span className="font-bold block text-on-surface">{config.upiId}</span>
                      <span className="text-xs text-secondary">{config.providerName} &bull; {config.isDefault ? "Primary Default" : "Secondary"}</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleDeleteUpi(config.id)}
                      className="text-red-500 hover:text-red-700 font-bold text-xs"
                    >
                      Delete
                    </button>
                  </div>
                ))}
                {upiConfigs.length === 0 && (
                  <p className="text-secondary text-xs">No UPI bank addresses registered yet.</p>
                )}
              </div>
            </div>

            <form onSubmit={handleAddUpi} className="border-t border-outline-variant pt-4 space-y-4">
              <h3 className="font-bold text-sm text-on-surface">Add New UPI Address</h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold mb-1">UPI ID / VPA</label>
                  <input type="text" placeholder="e.g. outlet@upi" value={newUpiId} onChange={(e) => setNewUpiId(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-xs font-semibold mb-1">Provider Name</label>
                  <select value={newProvider} onChange={(e) => setNewProvider(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm bg-white">
                    <option value="GPay">Google Pay (GPay)</option>
                    <option value="PhonePe">PhonePe</option>
                    <option value="Paytm">Paytm</option>
                    <option value="BHIM">BHIM UPI</option>
                  </select>
                </div>
              </div>
              <button type="submit" className="bg-primary-container text-white px-6 py-2.5 rounded-full font-bold text-xs">Add UPI Config</button>
            </form>
          </div>
        )}
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
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
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
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [restaurantId, setRestaurantId] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [editingCoupon, setEditingCoupon] = useState<Coupon | null>(null);

  // New Coupon Form fields
  const [code, setCode] = useState("");
  const [discount, setDiscount] = useState(0);
  const [type, setType] = useState<"percentage" | "flat">("flat");
  const [minOrder, setMinOrder] = useState(0);
  const [maxDiscount, setMaxDiscount] = useState(0);
  const [description, setDescription] = useState("");

  const loadCoupons = async () => {
    setLoading(true);
    try {
      const rest = await apiService.getMyRestaurant();
      if (rest) {
        setRestaurantId(Number(rest.id));
      }
      const data = await apiService.getCoupons();
      setCoupons(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCoupons();
  }, []);

  const handleCreateCoupon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code || discount <= 0) {
      toast.error("Please enter a valid coupon code and discount");
      return;
    }
    
    try {
      const now = new Date();
      const expiry = new Date();
      expiry.setDate(expiry.getDate() + 30); // 30 days validity
      
      const payload = {
        code: code.toUpperCase().trim(),
        description,
        couponType: type.toUpperCase(),
        value: discount,
        minOrderAmount: minOrder,
        maxDiscount: maxDiscount > 0 ? maxDiscount : null,
        validFrom: now.toISOString(),
        validTo: expiry.toISOString(),
        active: true,
        restaurantId
      };
      
      if (editingCoupon?.id) {
        await apiService.updateCoupon(editingCoupon.id, payload);
        toast.success("Promo coupon updated!");
      } else {
        await apiService.createCoupon(payload);
        toast.success("Promo coupon created!");
      }
      setCode("");
      setDiscount(0);
      setMinOrder(0);
      setMaxDiscount(0);
      setDescription("");
      setEditingCoupon(null);
      loadCoupons();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to create coupon");
    }
  };

  const handleEdit = (coupon: Coupon) => {
    setEditingCoupon(coupon);
    setCode(coupon.code);
    setDiscount(coupon.discount);
    setType(coupon.type);
    setMinOrder(coupon.minOrder);
    setMaxDiscount(coupon.maxDiscount || 0);
    setDescription(coupon.description || "");
  };

  const handleCancelEdit = () => {
    setEditingCoupon(null);
    setCode("");
    setDiscount(0);
    setMinOrder(0);
    setMaxDiscount(0);
    setDescription("");
  };

  const handleDelete = async (id?: string) => {
    if (!id) return;
    if (!window.confirm("Are you sure you want to delete this coupon?")) return;
    try {
      await apiService.deleteCoupon(id);
      toast.success("Coupon code deleted");
      loadCoupons();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete coupon");
    }
  };

  if (loading) return <div className="text-center py-12 text-secondary">Loading promo coupons...</div>;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Store Coupons & Offers</h2>
        <p className="text-secondary text-sm">Create and manage promo coupon codes for your customers</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        {/* Coupon Form */}
        <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 h-fit shadow-sm">
          <h3 className="font-bold text-lg text-on-surface">{editingCoupon ? "Edit Coupon" : "Create New Coupon"}</h3>
          <form onSubmit={handleCreateCoupon} className="space-y-3">
            <div>
              <label className="block text-xs font-semibold mb-1">Coupon Code</label>
              <input type="text" placeholder="e.g. EXTRA20" value={code} onChange={(e) => setCode(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Discount Type</label>
              <select value={type} onChange={(e) => setType(e.target.value as any)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm bg-white">
                <option value="flat">Flat Cash Discount (₹)</option>
                <option value="percentage">Percentage Discount (%)</option>
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold mb-1">Discount Value</label>
                <input type="number" placeholder="50" value={discount || ""} onChange={(e) => setDiscount(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1">Min Order Value (₹)</label>
                <input type="number" placeholder="199" value={minOrder || ""} onChange={(e) => setMinOrder(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Description</label>
              <input type="text" placeholder="Get Flat ₹50 off on orders above ₹199" value={description} onChange={(e) => setDescription(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" />
            </div>
            <button type="submit" className="w-full bg-primary-container text-white py-2 rounded-full font-bold text-sm hover:bg-primary transition-all">
              {editingCoupon ? "Update Coupon" : "Create Coupon"}
            </button>
            {editingCoupon && (
              <button type="button" onClick={handleCancelEdit} className="w-full bg-white border border-outline-variant text-secondary py-2 rounded-full font-bold text-sm">
                Cancel Edit
              </button>
            )}
          </form>
        </div>

        {/* Coupons List */}
        <div className="lg:col-span-2 bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm h-fit">
          <table className="w-full text-left">
            <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
              <tr>
                <th className="p-4">Coupon Code</th>
                <th className="p-4">Type</th>
                <th className="p-4">Discount</th>
                <th className="p-4">Min Order</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant text-sm">
              {coupons.map((coupon) => (
                <tr key={coupon.id} className="hover:bg-neutral-50/50">
                  <td className="p-4 font-bold text-on-surface">
                    <span className="bg-tertiary-container/10 text-tertiary text-xs font-extrabold px-2.5 py-1 rounded-md tracking-wider uppercase">{coupon.code}</span>
                    <span className="block text-xs text-secondary mt-1">{coupon.description}</span>
                  </td>
                  <td className="p-4 text-secondary uppercase">{coupon.type}</td>
                  <td className="p-4 font-bold">{coupon.type === "percentage" ? `${coupon.discount}%` : `₹${coupon.discount}`}</td>
                  <td className="p-4">₹{coupon.minOrder}</td>
                  <td className="p-4 text-center">
                    <button
                      type="button"
                      onClick={() => handleEdit(coupon)}
                      className="text-primary hover:text-primary-container font-bold mr-4"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(coupon.id)}
                      className="text-red-500 hover:text-red-700 font-bold"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {coupons.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-secondary">No custom coupons created yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

// 9. CUSTOMER REVIEWS
export const MerchantReviews: React.FC = () => {
  return (
    <div className="space-y-6 animate-fade-in">
      <h2 className="text-headline-md font-bold text-on-surface">Customer Reviews</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary shadow-sm">
        Review feedback loops are active. Partner reviews aggregated score is 4.6★.
      </div>
    </div>
  );
};

// 10. INVENTORY MANAGEMENT (NEW)
export const MerchantInventory: React.FC = () => {
  const [inventory, setInventory] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingItem, setEditingItem] = useState<any | null>(null);
  const [qty, setQty] = useState(0);
  const [threshold, setThreshold] = useState(0);

  const loadInventory = async () => {
    setLoading(true);
    try {
      const res = await api.get("/v1/restaurants/inventory?size=100");
      setInventory(res.data.data.content || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  const handleEditStock = (item: any) => {
    setEditingItem(item);
    setQty(item.quantity);
    setThreshold(item.lowStockThreshold);
  };

  const handleUpdateStock = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingItem) return;
    try {
      await api.put(`/v1/restaurants/inventory/items/${editingItem.foodItem?.id}`, {
        quantity: qty,
        lowStockThreshold: threshold
      });
      toast.success("Stock levels updated successfully!");
      setEditingItem(null);
      loadInventory();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update stock");
    }
  };

  if (loading) return <div className="text-center py-12 text-secondary">Loading inventory levels...</div>;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Inventory Management</h2>
        <p className="text-secondary text-sm">Track stock levels and low-stock warning indicators for your dishes</p>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left">
          <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
            <tr>
              <th className="p-4">Dish</th>
              <th className="p-4">Current Stock</th>
              <th className="p-4">Low Stock Warning Threshold</th>
              <th className="p-4">Status</th>
              <th className="p-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {inventory.map((item) => {
              const isLow = item.quantity <= item.lowStockThreshold;
              const isOut = item.quantity === 0;
              return (
                <tr key={item.id} className="hover:bg-neutral-50/50">
                  <td className="p-4 font-bold text-on-surface">{item.foodItem?.name}</td>
                  <td className="p-4 font-extrabold">{item.quantity}</td>
                  <td className="p-4 text-secondary">{item.lowStockThreshold} units</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                      isOut ? "bg-red-100 text-red-700" : isLow ? "bg-amber-100 text-amber-700" : "bg-green-100 text-green-700"
                    }`}>
                      {isOut ? "Out of Stock" : isLow ? "Low Stock" : "In Stock"}
                    </span>
                  </td>
                  <td className="p-4 text-center">
                    <button
                      type="button"
                      onClick={() => handleEditStock(item)}
                      className="text-primary hover:text-primary-container font-semibold"
                    >
                      Update Stock
                    </button>
                  </td>
                </tr>
              );
            })}
            {inventory.length === 0 && (
              <tr>
                <td colSpan={5} className="p-8 text-center text-secondary">No inventory items mapped. Add menu items first.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {editingItem && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl max-w-sm w-full space-y-4 shadow-xl">
            <h3 className="font-bold text-lg text-on-surface">Update Stock: {editingItem.foodItem?.name}</h3>
            <form onSubmit={handleUpdateStock} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1 text-secondary">Current Quantity</label>
                <input type="number" value={qty} onChange={(e) => setQty(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm font-semibold focus:outline-none" required />
              </div>
              <div>
                <label className="block text-sm font-semibold mb-1 text-secondary">Low-Stock Alert Threshold</label>
                <input type="number" value={threshold} onChange={(e) => setThreshold(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm font-semibold focus:outline-none" required />
              </div>
              <div className="flex gap-2 justify-end pt-2">
                <button type="button" onClick={() => setEditingItem(null)} className="px-4 py-2 bg-white border border-outline-variant rounded-full text-xs font-bold text-secondary">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-primary-container text-white rounded-full text-xs font-bold">Save Stock</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
