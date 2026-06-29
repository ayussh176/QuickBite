import React, { useState, useEffect } from "react";
import { apiService } from "../services/api/apiClient";
import type { Order } from "../services/api/apiClient";
import toast from "react-hot-toast";

// 1. DELIVERY DASHBOARD
export const DeliveryDashboard: React.FC = () => {
  const [pendingJobs, setPendingJobs] = useState<Order[]>([]);
  const [activeJob, setActiveJob] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      // 1. Fetch pending orders (ready for pickup)
      const pending = await apiService.getPendingDeliveries();
      setPendingJobs(pending);

      // 2. Fetch our own active jobs (assigned, picked up, etc.)
      const history = await apiService.getOrders(); // calls /v1/delivery/history
      const active = history.find(o => o.status === "preparing" || o.status === "picked_up");
      setActiveJob(active || null);
    } catch (e) {
      console.error("Error loading delivery jobs", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, []);

  const handleAcceptJob = async (orderId: string) => {
    try {
      const accepted = await apiService.acceptDelivery(orderId);
      setActiveJob(accepted);
      toast.success("Delivery request accepted! Proceed to pickup.");
      fetchJobs();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to accept delivery");
    }
  };

  const handleStatusUpdate = async () => {
    if (!activeJob) return;

    try {
      const nextStatus = activeJob.status === "preparing" ? "picked_up" : "delivered";
      const updated = await apiService.updateOrderStatus(activeJob.id, nextStatus);
      setActiveJob(nextStatus === "delivered" ? null : updated);
      toast.success(nextStatus === "picked_up" ? "Order picked up successfully!" : "Order delivered successfully!");
      fetchJobs();
    } catch (e: any) {
      toast.error(e.response?.data?.message || "Failed to update delivery status");
    }
  };

  if (loading) {
    return <div className="text-center py-12 text-secondary">Loading console...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Courier Console</h2>
        <p className="text-secondary text-sm">Manage active and pending deliveries</p>
      </div>

      {activeJob ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
          {/* Map display */}
          <div className="lg:col-span-2 bg-surface-container-low border border-outline-variant rounded-2xl h-80 flex flex-col items-center justify-center text-secondary relative overflow-hidden">
            <span className="material-symbols-outlined text-5xl mb-2 text-primary-container">map</span>
            <span className="font-bold text-sm">Delivery Router Map</span>
            <span className="text-xs">Active Route for Order #{activeJob.id}</span>
          </div>

          {/* Job Card */}
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 h-fit shadow-sm">
            <h3 className="font-bold text-lg text-on-surface">Active Task</h3>
            <div className="space-y-2 text-sm text-secondary">
              <div><span className="font-semibold text-on-surface">Order ID:</span> #{activeJob.id}</div>
              <div><span className="font-semibold text-on-surface">Pickup:</span> {activeJob.restaurantName}</div>
              <div><span className="font-semibold text-on-surface">Dropoff Address:</span> {activeJob.address}</div>
              <div><span className="font-semibold text-on-surface">Earnings:</span> ₹{activeJob.deliveryFee}</div>
              <div><span className="font-semibold text-on-surface">Status:</span> <span className="font-bold text-primary-container uppercase">{activeJob.status}</span></div>
            </div>
            <button
              onClick={handleStatusUpdate}
              className="w-full bg-primary-container text-white py-2.5 rounded-full font-bold text-sm hover:bg-primary transition-all"
            >
              {activeJob.status === "preparing" ? "Confirm Pickup" : "Confirm Dropoff"}
            </button>
          </div>
        </div>
      ) : (
        <div className="space-y-6">
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl text-center text-secondary">
            <span className="material-symbols-outlined text-4xl block mb-2 text-neutral-300">check_circle</span>
            No active delivery jobs. See pending orders below.
          </div>

          <h3 className="text-title-lg font-bold text-on-surface">Available Delivery Jobs ({pendingJobs.length})</h3>
          {pendingJobs.length === 0 ? (
            <p className="text-secondary text-sm">No pending deliveries in your area.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {pendingJobs.map((job) => (
                <div key={job.id} className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 shadow-sm flex flex-col justify-between">
                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="font-bold text-on-surface">#{job.id}</span>
                      <span className="text-xs font-bold text-tertiary bg-tertiary-container px-2 py-0.5 rounded-full uppercase">Ready</span>
                    </div>
                    <div className="text-sm text-secondary">
                      <p><span className="font-semibold text-on-surface">From:</span> {job.restaurantName}</p>
                      <p className="truncate"><span className="font-semibold text-on-surface">To:</span> {job.address}</p>
                      <p><span className="font-semibold text-on-surface">Fee:</span> ₹{job.deliveryFee}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => handleAcceptJob(job.id)}
                    className="w-full bg-primary text-white py-2 rounded-full font-bold text-sm hover:bg-primary-container transition-all"
                  >
                    Accept Job
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// 2. DELIVERY ORDERS HISTORY
export const DeliveryOrders: React.FC = () => {
  const [deliveries, setDeliveries] = useState<Order[]>([]);
  const [earnings, setEarnings] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadHistory = async () => {
      try {
        const history = await apiService.getOrders();
        setDeliveries(history.filter(o => o.status === "delivered" || o.status === "cancelled"));

        const earningsData = await apiService.getRiderEarnings();
        setEarnings(earningsData);
      } catch (e) {
        console.error("Error loading delivery history", e);
      } finally {
        setLoading(false);
      }
    };
    loadHistory();
  }, []);

  if (loading) {
    return <div className="text-center py-12 text-secondary">Loading history...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h2 className="text-headline-md font-bold text-on-surface">Delivery History</h2>
          <p className="text-secondary text-sm">Review your past deliveries and earnings</p>
        </div>
        {earnings && (
          <div className="bg-surface-container border border-outline-variant p-4 rounded-xl flex gap-6 text-sm">
            <div>
              <span className="block text-secondary text-xs">Total Earnings</span>
              <span className="font-extrabold text-lg text-primary-container">₹{earnings.totalEarnings || 0}</span>
            </div>
            <div className="border-l border-outline-variant pl-6">
              <span className="block text-secondary text-xs">Deliveries Completed</span>
              <span className="font-extrabold text-lg text-on-surface">{earnings.completedDeliveries || 0}</span>
            </div>
          </div>
        )}
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left">
          <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
            <tr>
              <th className="p-4">Order ID</th>
              <th className="p-4">Date</th>
              <th className="p-4">Restaurant</th>
              <th className="p-4">Earning</th>
              <th className="p-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {deliveries.map((d) => (
              <tr key={d.id}>
                <td className="p-4 font-bold text-on-surface">#{d.id}</td>
                <td className="p-4 text-secondary">{d.date}</td>
                <td className="p-4 text-secondary">{d.restaurantName}</td>
                <td className="p-4 font-semibold">₹{d.deliveryFee}</td>
                <td className="p-4">
                  <span className={`font-bold text-xs ${d.status === "delivered" ? "text-tertiary" : "text-red-500"}`}>
                    {d.status.toUpperCase()}
                  </span>
                </td>
              </tr>
            ))}
            {deliveries.length === 0 && (
              <tr>
                <td colSpan={5} className="p-8 text-center text-secondary">No delivery history found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// 3. DELIVERY PROFILE
export const DeliveryProfile: React.FC = () => {
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiService.getRiderProfile().then((data) => {
      setProfile(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-center py-12 text-secondary">Loading profile...</div>;

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Rider Profile</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 max-w-md shadow-sm">
        <div>
          <label className="block text-sm font-semibold mb-1 text-secondary">Rider Name</label>
          <input type="text" disabled value={profile ? `${profile.firstName} ${profile.lastName}` : "Ramesh Sharma"} className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-neutral-50 text-on-surface font-semibold" />
        </div>
        <div>
          <label className="block text-sm font-semibold mb-1 text-secondary">Vehicle Details</label>
          <input type="text" disabled value={profile && profile.vehicle ? `${profile.vehicle.model} (${profile.vehicle.registrationNumber})` : "Motorcycle (UP16-CZ-1234)"} className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-neutral-50 text-on-surface font-semibold" />
        </div>
        <div className="flex gap-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-secondary">Rating</label>
            <div className="font-extrabold text-lg text-primary-container">{profile ? profile.avgRating : "4.9"} ⭐</div>
          </div>
          <div className="border-l border-outline-variant pl-4">
            <label className="block text-sm font-semibold mb-1 text-secondary">KYC Status</label>
            <div className="font-bold text-sm text-tertiary bg-tertiary-container px-2 py-0.5 rounded-full w-fit">VERIFIED</div>
          </div>
        </div>
      </div>
    </div>
  );
};
