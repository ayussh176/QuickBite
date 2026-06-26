import React, { useState } from "react";
import type { OnboardingRequest, SupportTicket } from "../services/api/mockData";
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
  const [requests, setRequests] = useState<OnboardingRequest[]>(() =>
    JSON.parse(localStorage.getItem("qb_onboarding") || "[]")
  );

  const handleAction = (id: string, status: "approved" | "rejected") => {
    const updated = requests.map(r => r.id === id ? { ...r, status } : r);
    setRequests(updated);
    localStorage.setItem("qb_onboarding", JSON.stringify(updated));
    toast.success(`Merchant Request marked as ${status.toUpperCase()}`);
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Approvals & Onboarding</h2>
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container-low border-b border-outline-variant text-sm font-semibold text-secondary">
              <th className="p-4">Restaurant</th>
              <th className="p-4">Owner</th>
              <th className="p-4">License</th>
              <th className="p-4">Status</th>
              <th className="p-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {requests.map((r) => (
              <tr key={r.id} className="hover:bg-surface-container-low/50">
                <td className="p-4 font-bold text-on-surface">{r.restaurantName}</td>
                <td className="p-4 text-secondary">{r.ownerName} ({r.phone})</td>
                <td className="p-4 font-semibold">{r.licenseNumber}</td>
                <td className="p-4">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    r.status === "approved" ? "bg-green-100 text-green-700" : r.status === "rejected" ? "bg-red-100 text-red-700" : "bg-yellow-100 text-yellow-700"
                  }`}>
                    {r.status.toUpperCase()}
                  </span>
                </td>
                <td className="p-4 text-center flex justify-center gap-2">
                  {r.status === "pending" && (
                    <>
                      <button 
                        onClick={() => handleAction(r.id, "approved")}
                        className="bg-tertiary-container text-white text-xs font-bold px-3 py-1 rounded-full hover:scale-105 transition-all"
                      >
                        Approve
                      </button>
                      <button 
                        onClick={() => handleAction(r.id, "rejected")}
                        className="bg-white border border-red-200 text-red-600 text-xs font-bold px-3 py-1 rounded-full hover:bg-red-50"
                      >
                        Reject
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
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
  const [tickets, setTickets] = useState<SupportTicket[]>(() =>
    JSON.parse(localStorage.getItem("qb_tickets") || "[]")
  );

  const handleResolve = (id: string) => {
    const updated = tickets.map(t => t.id === id ? { ...t, status: "resolved" as const } : t);
    setTickets(updated);
    localStorage.setItem("qb_tickets", JSON.stringify(updated));
    toast.success("Support Ticket marked as resolved");
  };

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Support & Disputes</h2>
      <div className="space-y-4">
        {tickets.map((t) => (
          <div key={t.id} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-3 shadow-sm">
            <div className="flex justify-between items-center border-b border-outline-variant pb-2">
              <div>
                <span className="font-bold text-on-surface text-lg block">{t.subject}</span>
                <span className="text-xs text-secondary">{t.userName} ({t.userType}) &bull; Ticket #{t.id}</span>
              </div>
              <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                t.status === "resolved" ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"
              }`}>
                {t.status.toUpperCase()}
              </span>
            </div>
            <p className="text-secondary text-sm">{t.message}</p>
            {t.status === "open" && (
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
      </div>
    </div>
  );
};
