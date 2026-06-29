import React, { useState, useEffect } from "react";
import { apiService } from "../services/api/apiClient";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import toast from "react-hot-toast";

const mockGmvData = [
  { month: "Jan", gmv: 150000 },
  { month: "Feb", gmv: 210000 },
  { month: "Mar", gmv: 195000 },
  { month: "Apr", gmv: 280000 },
  { month: "May", gmv: 340000 },
  { month: "Jun", gmv: 420000 }
];

// 1. ADMIN DASHBOARD
export const AdminDashboard: React.FC = () => {
  const stats = [
    { label: "Active Customers", value: "14,820", icon: "group", change: "+8% this month" },
    { label: "Active Merchants", value: "245", icon: "storefront", change: "12 pending approvals" },
    { label: "Active Couriers", value: "1,105", icon: "sports_motorsports", change: "82% online" },
    { label: "Platform GMV (MTD)", value: "₹4.2M", icon: "account_balance_wallet", change: "+18.2% vs last month" }
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Admin Control Console</h2>
        <p className="text-secondary text-sm">Global platform logs and health overview</p>
      </div>

      {/* Bento Grid Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {stats.map((stat, i) => (
          <div key={i} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl relative overflow-hidden shadow-sm">
            <span className="material-symbols-outlined absolute right-4 top-4 text-primary-container bg-primary-container/10 p-2 rounded-xl text-2xl">
              {stat.icon}
            </span>
            <span className="text-xs font-semibold text-secondary block">{stat.label}</span>
            <span className="text-3xl font-extrabold text-on-surface block mt-2">{stat.value}</span>
            <span className="text-xs text-tertiary font-bold block mt-1">{stat.change}</span>
          </div>
        ))}
      </div>

      {/* Platform Analytics Area Chart */}
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4">
        <h3 className="font-bold text-lg text-on-surface">Platform Growth Trend (Gross Merchandise Value)</h3>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={mockGmvData}>
              <defs>
                <linearGradient id="colorGmv" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#00b050" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#00b050" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Area type="monotone" dataKey="gmv" stroke="#00b050" strokeWidth={3} fillOpacity={1} fill="url(#colorGmv)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

// 2. ADMIN ONBOARDING
export const AdminOnboarding: React.FC = () => {
  const [requests, setRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchOnboarding = async () => {
    setLoading(true);
    try {
      const pending = await apiService.getPendingOnboarding();
      setRequests(pending);
    } catch (e) {
      console.error("Error loading onboarding requests", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOnboarding();
  }, []);

  const handleAction = async (id: string, approve: boolean) => {
    try {
      await apiService.verifyRestaurant(id, approve);
      toast.success(`Merchant Request marked as ${approve ? "APPROVED" : "REJECTED"}`);
      fetchOnboarding();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update onboarding request");
    }
  };

  if (loading) {
    return <div className="text-center py-12 text-secondary">Loading requests...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Approvals & Onboarding</h2>
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
              <th className="p-4">Restaurant</th>
              <th className="p-4">Owner Email</th>
              <th className="p-4">License</th>
              <th className="p-4">Status</th>
              <th className="p-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {requests.map((r) => (
              <tr key={r.id} className="hover:bg-surface-container-low/50">
                <td className="p-4 font-bold text-on-surface">{r.name}</td>
                <td className="p-4 text-secondary">{r.email || "merchant@quickbite.com"} ({r.phone})</td>
                <td className="p-4 font-semibold">{r.fssaiLicense || "FSSAI-PENDING"}</td>
                <td className="p-4">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    r.status === "APPROVED" ? "bg-green-100 text-green-700" : r.status === "REJECTED" ? "bg-red-100 text-red-700" : "bg-yellow-100 text-yellow-700"
                  }`}>
                    {r.status}
                  </span>
                </td>
                <td className="p-4 text-center flex justify-center gap-2">
                  {r.status === "PENDING_APPROVAL" && (
                    <>
                      <button 
                        onClick={() => handleAction(r.id, true)}
                        className="bg-tertiary-container text-white text-xs font-bold px-3 py-1 rounded-full hover:scale-105 transition-all"
                      >
                        Approve
                      </button>
                      <button 
                        onClick={() => handleAction(r.id, false)}
                        className="bg-white border border-red-200 text-red-600 text-xs font-bold px-3 py-1 rounded-full hover:bg-red-50"
                      >
                        Reject
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
            {requests.length === 0 && (
              <tr>
                <td colSpan={5} className="p-8 text-center text-secondary">No pending onboarding applications.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// 3. GLOBAL ANALYTICS
export const AdminAnalytics: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Global Analytics & Reports</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center py-12 text-secondary shadow-sm">
        <span className="material-symbols-outlined text-4xl block mb-2">analytics</span>
        Platform analytics pipeline is live. Tracking active order dispatch rates and cohort performance.
      </div>
    </div>
  );
};

// 4. SUPPORT & DISPUTES
export const AdminSupport: React.FC = () => {
  const [tickets, setTickets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const data = await apiService.getComplaints();
      setTickets(data);
    } catch (e) {
      console.error("Error loading complaints", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const handleResolve = async (id: string) => {
    const note = window.prompt("Enter resolution details:", "Resolved issue with refund.");
    if (note === null) return;
    
    try {
      await apiService.resolveComplaint(id, note);
      toast.success("Support Ticket marked as resolved");
      fetchTickets();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to resolve complaint");
    }
  };

  if (loading) {
    return <div className="text-center py-12 text-secondary">Loading support tickets...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Support & Disputes</h2>
      <div className="space-y-4">
        {tickets.map((t) => (
          <div key={t.id} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-3 shadow-sm">
            <div className="flex justify-between items-center border-b border-outline-variant pb-2">
              <div>
                <span className="font-bold text-on-surface text-lg block">{t.subject}</span>
                <span className="text-xs text-secondary">{t.customerName || "Customer"} &bull; Ticket #{t.id} &bull; Order #{t.orderNumber || "N/A"}</span>
              </div>
              <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                t.status === "RESOLVED" ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"
              }`}>
                {t.status}
              </span>
            </div>
            <p className="text-secondary text-sm">{t.description}</p>
            {t.resolutionDetails && (
              <div className="bg-neutral-50 p-3 rounded-lg text-xs text-on-surface">
                <span className="font-semibold block mb-1">Resolution Details:</span>
                {t.resolutionDetails}
              </div>
            )}
            {t.status === "PENDING" && (
              <div className="flex justify-end pt-2">
                <button 
                  onClick={() => handleResolve(t.id)}
                  className="bg-primary-container text-white font-bold px-4 py-1.5 rounded-full text-xs hover:bg-primary"
                >
                  Mark as Resolved
                </button>
              </div>
            )}
          </div>
        ))}
        {tickets.length === 0 && (
          <div className="bg-surface-container-lowest border border-outline-variant p-8 rounded-2xl text-center text-secondary">
            No support tickets or complaints filed.
          </div>
        )}
      </div>
    </div>
  );
};

// 5. USER MANAGEMENT
export const AdminUsers: React.FC = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState("");
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await apiService.getAdminUsers({
        search,
        role: role || undefined,
        status: status || undefined,
        page,
        size: 10
      });
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      console.error(e);
      toast.error("Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, role, status]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchUsers();
  };

  const handleStatusChange = async (userId: string | number, newStatus: string) => {
    try {
      await apiService.updateAdminUserStatus(userId, newStatus);
      toast.success("User status updated successfully");
      fetchUsers();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update status");
    }
  };

  const handleRoleChange = async (userId: string | number, newRole: string) => {
    try {
      await apiService.updateAdminUserRole(userId, newRole);
      toast.success("User role updated successfully");
      fetchUsers();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update role");
    }
  };

  const handleDeleteUser = async (userId: string | number) => {
    if (!window.confirm("Are you sure you want to delete this user? This cannot be undone.")) return;
    try {
      await apiService.deleteAdminUser(userId);
      toast.success("User deleted successfully");
      fetchUsers();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete user");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">User Management</h2>
        <p className="text-secondary text-sm">Control platform user credentials, roles, and status</p>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant p-4 rounded-2xl flex flex-wrap gap-4 items-center justify-between shadow-sm">
        <form onSubmit={handleSearchSubmit} className="flex gap-2 w-full md:w-auto">
          <input
            type="text"
            placeholder="Search email or phone..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="px-4 py-2 border border-outline-variant rounded-xl text-sm focus:outline-none w-64 bg-surface"
          />
          <button type="submit" className="bg-primary-container text-white px-4 py-2 rounded-xl text-sm font-bold">
            Search
          </button>
        </form>

        <div className="flex gap-4 items-center w-full md:w-auto justify-end">
          <select value={role} onChange={(e) => { setRole(e.target.value); setPage(0); }} className="px-3 py-2 border border-outline-variant rounded-xl text-sm bg-surface">
            <option value="">All Roles</option>
            <option value="CUSTOMER">Customer</option>
            <option value="RESTAURANT">Restaurant Owner</option>
            <option value="DELIVERY">Delivery Partner</option>
            <option value="ADMIN">Admin</option>
          </select>

          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} className="px-3 py-2 border border-outline-variant rounded-xl text-sm bg-surface">
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="PENDING_VERIFICATION">Pending Verification</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-12 text-secondary">Loading users...</div>
      ) : (
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
                <th className="p-4">ID</th>
                <th className="p-4">Email</th>
                <th className="p-4">Phone</th>
                <th className="p-4">Role</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant text-sm">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-surface-container-low/50">
                  <td className="p-4 font-semibold text-secondary">#{u.id}</td>
                  <td className="p-4 font-bold text-on-surface">{u.email}</td>
                  <td className="p-4 text-secondary">{u.phone || "N/A"}</td>
                  <td className="p-4">
                    <select
                      value={u.role}
                      onChange={(e) => handleRoleChange(u.id, e.target.value)}
                      disabled={u.role === "ADMIN"}
                      className="px-2 py-1 border border-outline-variant rounded-lg text-xs bg-surface"
                    >
                      <option value="CUSTOMER">CUSTOMER</option>
                      <option value="RESTAURANT">RESTAURANT</option>
                      <option value="DELIVERY">DELIVERY</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td className="p-4">
                    <select
                      value={u.status}
                      onChange={(e) => handleStatusChange(u.id, e.target.value)}
                      disabled={u.role === "ADMIN"}
                      className={`px-2 py-1 border border-outline-variant rounded-lg text-xs font-semibold ${
                        u.status === "ACTIVE" ? "text-green-700 bg-green-50" : u.status === "SUSPENDED" ? "text-red-700 bg-red-50" : "text-yellow-700 bg-yellow-50"
                      }`}
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                      <option value="PENDING_VERIFICATION">PENDING</option>
                      <option value="SUSPENDED">SUSPENDED</option>
                    </select>
                  </td>
                  <td className="p-4 text-center">
                    <button
                      onClick={() => handleDeleteUser(u.id)}
                      disabled={u.role === "ADMIN"}
                      className="text-red-500 hover:text-red-700 font-bold text-xs disabled:opacity-50"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-secondary">No users found.</td>
                </tr>
              )}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="p-4 border-t border-outline-variant flex justify-between items-center text-xs">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Previous</button>
              <span>Page {page + 1} of {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Next</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// 6. RESTAURANT MANAGEMENT
export const AdminRestaurants: React.FC = () => {
  const [restaurants, setRestaurants] = useState<any[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [editingRestaurant, setEditingRestaurant] = useState<any | null>(null);

  // Edit fields
  const [name, setName] = useState("");
  const [cuisineType, setCuisineType] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [minOrder, setMinOrder] = useState(0);
  const [deliveryFee, setDeliveryFee] = useState(0);
  const [estTime, setEstTime] = useState(30);
  const [addressLine1, setAddressLine1] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [zip, setZip] = useState("");

  const fetchRestaurants = async () => {
    setLoading(true);
    try {
      const data = await apiService.getAdminRestaurants({
        search,
        status: status || undefined,
        page,
        size: 10
      });
      setRestaurants(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      console.error(e);
      toast.error("Failed to load restaurants");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRestaurants();
  }, [page, status]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchRestaurants();
  };

  const handleStatusUpdate = async (id: string | number, newStatus: string) => {
    try {
      await apiService.verifyRestaurant(id, newStatus === "APPROVED");
      toast.success(`Restaurant status marked as ${newStatus}`);
      fetchRestaurants();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update restaurant status");
    }
  };

  const handleDeleteRestaurant = async (id: string | number) => {
    if (!window.confirm("Are you sure you want to delete this restaurant? This cannot be undone.")) return;
    try {
      await apiService.deleteAdminRestaurant(id);
      toast.success("Restaurant deleted successfully");
      fetchRestaurants();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to delete restaurant");
    }
  };

  const handleOpenEdit = (r: any) => {
    setEditingRestaurant(r);
    setName(r.name || "");
    setCuisineType(r.cuisineType || "");
    setPhone(r.phone || "");
    setEmail(r.email || "");
    setMinOrder(r.minOrderAmount || 0);
    setDeliveryFee(r.deliveryFee || 0);
    setEstTime(r.estimatedDeliveryTime || 30);
    setAddressLine1(r.address?.addressLine1 || "");
    setCity(r.address?.city || "");
    setState(r.address?.state || "");
    setZip(r.address?.zipCode || "");
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingRestaurant) return;
    try {
      await apiService.updateAdminRestaurantProfile(editingRestaurant.id, {
        name,
        cuisineType,
        phone,
        email,
        minOrderAmount: minOrder,
        deliveryFee,
        estimatedDeliveryTime: estTime,
        address: {
          addressLine1,
          city,
          state,
          zipCode: zip
        }
      });
      toast.success("Restaurant profile updated successfully");
      setEditingRestaurant(null);
      fetchRestaurants();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update restaurant details");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Restaurant Management</h2>
        <p className="text-secondary text-sm">Verify new merchants, edit details, or disable outlets</p>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant p-4 rounded-2xl flex flex-wrap gap-4 items-center justify-between shadow-sm">
        <form onSubmit={handleSearchSubmit} className="flex gap-2 w-full md:w-auto">
          <input
            type="text"
            placeholder="Search name, phone, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="px-4 py-2 border border-outline-variant rounded-xl text-sm focus:outline-none w-64 bg-surface"
          />
          <button type="submit" className="bg-primary-container text-white px-4 py-2 rounded-xl text-sm font-bold">
            Search
          </button>
        </form>

        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} className="px-3 py-2 border border-outline-variant rounded-xl text-sm bg-surface">
          <option value="">All Statuses</option>
          <option value="PENDING_APPROVAL">Pending Approval</option>
          <option value="APPROVED">Approved / Active</option>
          <option value="REJECTED">Rejected</option>
          <option value="SUSPENDED">Suspended</option>
        </select>
      </div>

      {loading ? (
        <div className="text-center py-12 text-secondary">Loading restaurants...</div>
      ) : (
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
                <th className="p-4">Restaurant</th>
                <th className="p-4">Contact</th>
                <th className="p-4">Cuisine</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant text-sm">
              {restaurants.map((r) => (
                <tr key={r.id} className="hover:bg-surface-container-low/50">
                  <td className="p-4">
                    <span className="font-bold block text-on-surface">{r.name}</span>
                    <span className="text-xs text-secondary">{r.address?.city || "N/A"} &bull; Min Order: ₹{r.minOrderAmount}</span>
                  </td>
                  <td className="p-4">
                    <span className="block">{r.email}</span>
                    <span className="text-xs text-secondary">{r.phone || "No phone"}</span>
                  </td>
                  <td className="p-4 font-semibold text-secondary">{r.cuisineType || "General"}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                      r.status === "APPROVED" ? "bg-green-100 text-green-700" : r.status === "PENDING_APPROVAL" ? "bg-yellow-100 text-yellow-700" : "bg-red-100 text-red-700"
                    }`}>
                      {r.status}
                    </span>
                  </td>
                  <td className="p-4 text-center space-x-2">
                    <button onClick={() => handleOpenEdit(r)} className="text-primary hover:underline font-bold text-xs">Edit</button>
                    {r.status === "PENDING_APPROVAL" ? (
                      <>
                        <button onClick={() => handleStatusUpdate(r.id, "APPROVED")} className="text-green-600 hover:underline font-bold text-xs">Approve</button>
                        <button onClick={() => handleStatusUpdate(r.id, "REJECTED")} className="text-red-600 hover:underline font-bold text-xs">Reject</button>
                      </>
                    ) : (
                      <button onClick={() => handleStatusUpdate(r.id, r.status === "APPROVED" ? "SUSPENDED" : "APPROVED")} className="text-amber-600 hover:underline font-bold text-xs">
                        {r.status === "APPROVED" ? "Suspend" : "Activate"}
                      </button>
                    )}
                    <button onClick={() => handleDeleteRestaurant(r.id)} className="text-red-500 hover:underline font-bold text-xs">Delete</button>
                  </td>
                </tr>
              ))}
              {restaurants.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-secondary">No restaurants found.</td>
                </tr>
              )}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="p-4 border-t border-outline-variant flex justify-between items-center text-xs">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Previous</button>
              <span>Page {page + 1} of {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Next</button>
            </div>
          )}
        </div>
      )}

      {/* Editing Dialog Modal */}
      {editingRestaurant && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl max-w-2xl w-full space-y-4 shadow-xl max-h-[90vh] overflow-y-auto">
            <h3 className="font-bold text-lg text-on-surface">Edit Restaurant: {editingRestaurant.name}</h3>
            <form onSubmit={handleSaveEdit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Restaurant Name</label>
                  <input type="text" value={name} onChange={(e) => setName(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Cuisine Specialties</label>
                  <input type="text" value={cuisineType} onChange={(e) => setCuisineType(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" placeholder="e.g. Burgers, Pizza" required />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Contact Phone</label>
                  <input type="text" value={phone} onChange={(e) => setPhone(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Contact Email</label>
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Min Order (₹)</label>
                  <input type="number" value={minOrder} onChange={(e) => setMinOrder(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Delivery Fee (₹)</label>
                  <input type="number" value={deliveryFee} onChange={(e) => setDeliveryFee(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Prep Time (mins)</label>
                  <input type="number" value={estTime} onChange={(e) => setEstTime(Number(e.target.value))} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
              </div>

              <div className="border-t border-outline-variant pt-2 space-y-4">
                <h4 className="font-semibold text-sm">Location Address</h4>
                <div>
                  <label className="block text-xs font-semibold mb-1 text-secondary">Address Line 1</label>
                  <input type="text" value={addressLine1} onChange={(e) => setAddressLine1(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-xs font-semibold mb-1 text-secondary">City</label>
                    <input type="text" value={city} onChange={(e) => setCity(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold mb-1 text-secondary">State</label>
                    <input type="text" value={state} onChange={(e) => setState(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold mb-1 text-secondary">Zip Code</label>
                    <input type="text" value={zip} onChange={(e) => setZip(e.target.value)} className="w-full px-4 py-2 border border-outline-variant rounded-xl text-sm" required />
                  </div>
                </div>
              </div>

              <div className="flex gap-2 justify-end pt-2">
                <button type="button" onClick={() => setEditingRestaurant(null)} className="px-4 py-2 bg-white border border-outline-variant rounded-full text-xs font-bold text-secondary">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-primary-container text-white rounded-full text-xs font-bold">Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// 7. GLOBAL ORDER MANAGEMENT
export const AdminOrders: React.FC = () => {
  const [orders, setOrders] = useState<any[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [viewingTimelineOrder, setViewingTimelineOrder] = useState<any | null>(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const data = await apiService.getAdminOrders({
        search,
        status: status || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        page,
        size: 10
      });
      setOrders(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      console.error(e);
      toast.error("Failed to load platform orders");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [page, status]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchOrders();
  };

  const handleClearFilters = () => {
    setSearch("");
    setStatus("");
    setStartDate("");
    setEndDate("");
    setPage(0);
    setTimeout(fetchOrders, 0);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "DELIVERED": return "bg-green-100 text-green-700";
      case "CANCELLED": return "bg-red-100 text-red-700";
      case "DISPATCHED": return "bg-purple-100 text-purple-700";
      case "PREPARING": return "bg-blue-100 text-blue-700";
      default: return "bg-yellow-100 text-yellow-700";
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Global Order Logs</h2>
        <p className="text-secondary text-sm">Monitor platform food dispatch sequences and client billing cycles</p>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant p-4 rounded-2xl space-y-4 shadow-sm">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          <div>
            <label className="block text-xs font-semibold mb-1 text-secondary">Search Keyword</label>
            <input
              type="text"
              placeholder="Order ID, Customer, Restaurant..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="px-4 py-2 border border-outline-variant rounded-xl text-sm focus:outline-none w-full bg-surface"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1 text-secondary">Order Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)} className="px-3 py-2 border border-outline-variant rounded-xl text-sm bg-surface w-full">
              <option value="">All Statuses</option>
              <option value="CREATED">CREATED</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="PREPARING">PREPARING</option>
              <option value="READY_FOR_PICKUP">READY FOR PICKUP</option>
              <option value="DISPATCHED">DISPATCHED</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1 text-secondary">Start Date</label>
            <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="px-3 py-1.5 border border-outline-variant rounded-xl text-sm w-full bg-surface animate-fade-in" />
          </div>

          <div className="flex gap-2">
            <button type="submit" className="flex-1 bg-primary-container text-white px-4 py-2 rounded-xl text-sm font-bold">
              Filter Log
            </button>
            <button type="button" onClick={handleClearFilters} className="bg-surface border border-outline-variant text-secondary px-4 py-2 rounded-xl text-sm font-bold">
              Reset
            </button>
          </div>
        </form>
      </div>

      {loading ? (
        <div className="text-center py-12 text-secondary">Loading orders...</div>
      ) : (
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
                <th className="p-4">Order #</th>
                <th className="p-4">Restaurant</th>
                <th className="p-4">Bill Total</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-center">Timeline</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant text-sm">
              {orders.map((o) => (
                <tr key={o.id} className="hover:bg-surface-container-low/50">
                  <td className="p-4 font-bold text-on-surface">#{o.id}</td>
                  <td className="p-4 font-bold">{o.restaurantName}</td>
                  <td className="p-4 font-extrabold text-on-surface">₹{o.total}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${getStatusColor(o.status)}`}>
                      {o.status}
                    </span>
                  </td>
                  <td className="p-4 text-center">
                    <button
                      onClick={() => setViewingTimelineOrder(o)}
                      className="text-primary hover:underline font-bold text-xs"
                    >
                      Track Steps
                    </button>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-secondary">No platform orders found.</td>
                </tr>
              )}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="p-4 border-t border-outline-variant flex justify-between items-center text-xs">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Previous</button>
              <span>Page {page + 1} of {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="px-3 py-1 bg-surface border border-outline-variant rounded-xl disabled:opacity-50">Next</button>
            </div>
          )}
        </div>
      )}

      {/* Timeline Tracker Dialog Modal */}
      {viewingTimelineOrder && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl max-w-md w-full space-y-4 shadow-xl">
            <div className="flex justify-between items-center border-b border-outline-variant pb-2">
              <h3 className="font-bold text-lg text-on-surface">Order Timeline #{viewingTimelineOrder.id}</h3>
              <button onClick={() => setViewingTimelineOrder(null)} className="text-secondary hover:text-black">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            
            <div className="space-y-4 text-sm relative border-l-2 border-outline-variant pl-4 ml-2 animate-fade-in">
              <div className="relative">
                <span className="absolute -left-[25px] top-1.5 w-3.5 h-3.5 rounded-full bg-green-500 border-2 border-white"></span>
                <p className="font-bold text-on-surface">Order Placed</p>
                <p className="text-xs text-secondary">{viewingTimelineOrder.date || "Just now"}</p>
              </div>

              <div className="relative">
                <span className={`absolute -left-[25px] top-1.5 w-3.5 h-3.5 rounded-full border-2 border-white ${
                  ["CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "DISPATCHED", "DELIVERED"].includes(viewingTimelineOrder.status)
                    ? "bg-green-500" : "bg-neutral-300"
                }`}></span>
                <p className="font-bold text-on-surface">Confirmed by Restaurant</p>
                <p className="text-xs text-secondary">Prepped & checked</p>
              </div>

              <div className="relative">
                <span className={`absolute -left-[25px] top-1.5 w-3.5 h-3.5 rounded-full border-2 border-white ${
                  ["PREPARING", "READY_FOR_PICKUP", "DISPATCHED", "DELIVERED"].includes(viewingTimelineOrder.status)
                    ? "bg-green-500" : "bg-neutral-300"
                }`}></span>
                <p className="font-bold text-on-surface">Cooking & Preparing</p>
                <p className="text-xs text-secondary">Kitchen active</p>
              </div>

              <div className="relative">
                <span className={`absolute -left-[25px] top-1.5 w-3.5 h-3.5 rounded-full border-2 border-white ${
                  ["DISPATCHED", "DELIVERED"].includes(viewingTimelineOrder.status)
                    ? "bg-green-500" : "bg-neutral-300"
                }`}></span>
                <p className="font-bold text-on-surface">Courier Dispatched</p>
                <p className="text-xs text-secondary">Out for delivery</p>
              </div>

              <div className="relative">
                <span className={`absolute -left-[25px] top-1.5 w-3.5 h-3.5 rounded-full border-2 border-white ${
                  viewingTimelineOrder.status === "DELIVERED"
                    ? "bg-green-500" : viewingTimelineOrder.status === "CANCELLED" ? "bg-red-500" : "bg-neutral-300"
                }`}></span>
                <p className="font-bold text-on-surface">
                  {viewingTimelineOrder.status === "CANCELLED" ? "Order Cancelled" : "Delivered"}
                </p>
                <p className="text-xs text-secondary">End status milestone reached</p>
              </div>
            </div>

            <div className="pt-2 text-xs border-t border-outline-variant space-y-1">
              <div className="flex justify-between">
                <span className="text-secondary font-semibold">Restaurant:</span>
                <span className="font-bold text-on-surface">{viewingTimelineOrder.restaurantName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-secondary font-semibold">Payment:</span>
                <span className="font-bold text-on-surface">Wallet Checkout</span>
              </div>
              <div className="flex justify-between">
                <span className="text-secondary font-semibold">Bill Subtotal:</span>
                <span className="font-bold text-on-surface">₹{viewingTimelineOrder.subtotal}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-secondary font-semibold">Delivery Fee:</span>
                <span className="font-bold text-on-surface">₹{viewingTimelineOrder.deliveryFee}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-secondary font-semibold">Net Charged:</span>
                <span className="font-extrabold text-primary">₹{viewingTimelineOrder.total}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
