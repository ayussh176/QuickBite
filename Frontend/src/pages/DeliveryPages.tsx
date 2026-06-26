import React, { useState } from "react";
import toast from "react-hot-toast";

// 1. DELIVERY DASHBOARD
export const DeliveryDashboard: React.FC = () => {
  const [activeJob, setActiveJob] = useState<boolean>(true);
  const [jobStatus, setJobStatus] = useState<"assigned" | "picked_up" | "completed">("assigned");

  const handleStatusUpdate = () => {
    if (jobStatus === "assigned") {
      setJobStatus("picked_up");
      toast.success("Order picked up from The Burger House");
    } else if (jobStatus === "picked_up") {
      setJobStatus("completed");
      setActiveJob(false);
      toast.success("Order delivered to customer successfully!");
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-md font-bold text-on-surface">Courier Console</h2>
        <p className="text-secondary text-sm">Manage active delivery orders</p>
      </div>

      {activeJob ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
          {/* Map display */}
          <div className="lg:col-span-2 bg-surface-container-low border border-outline-variant rounded-2xl h-80 flex flex-col items-center justify-center text-secondary relative overflow-hidden">
            <span className="material-symbols-outlined text-5xl mb-2 text-primary-container">map</span>
            <span className="font-bold text-sm">Delivery Router Map</span>
            <span className="text-xs">Navigating from Noida Sector 18 to Sector 44</span>
          </div>

          {/* Job Card */}
          <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 h-fit">
            <h3 className="font-bold text-lg text-on-surface">Active Task</h3>
            <div className="space-y-2 text-sm text-secondary">
              <div><span className="font-semibold text-on-surface">Pickup:</span> The Burger House</div>
              <div><span className="font-semibold text-on-surface">Dropoff:</span> H-15, Sector 44, Noida</div>
              <div><span className="font-semibold text-on-surface">Earnings:</span> ₹45 + ₹15 Tip</div>
            </div>
            <button
              onClick={handleStatusUpdate}
              className="w-full bg-primary-container text-white py-2 rounded-full font-bold text-sm hover:bg-primary transition-all"
            >
              {jobStatus === "assigned" ? "Confirm Pickup" : "Confirm Dropoff"}
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-surface-container-lowest border border-outline-variant p-8 rounded-2xl text-center text-secondary">
          <span className="material-symbols-outlined text-4xl block mb-2 text-neutral-300">check_circle</span>
          No active delivery jobs. Waiting for new assignments...
        </div>
      )}
    </div>
  );
};

// 2. DELIVERY ORDERS HISTORY
export const DeliveryOrders: React.FC = () => {
  const deliveries = [
    { id: "QB-88102", date: "2026-06-25", amount: 55, status: "completed" },
    { id: "QB-88094", date: "2026-06-24", amount: 60, status: "completed" }
  ];

  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Delivery History</h2>
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left">
          <thead className="bg-surface-container-low text-sm font-semibold text-secondary">
            <tr>
              <th className="p-4">Order ID</th>
              <th className="p-4">Date</th>
              <th className="p-4">Earning</th>
              <th className="p-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant text-sm">
            {deliveries.map((d) => (
              <tr key={d.id}>
                <td className="p-4 font-bold text-on-surface">#{d.id}</td>
                <td className="p-4 text-secondary">{d.date}</td>
                <td className="p-4 font-semibold">₹{d.amount}</td>
                <td className="p-4"><span className="text-tertiary font-bold text-xs">COMPLETED</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// 3. DELIVERY PROFILE
export const DeliveryProfile: React.FC = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-headline-md font-bold text-on-surface">Rider Profile</h2>
      <div className="bg-surface-container-lowest border border-outline-variant p-6 rounded-2xl space-y-4 max-w-md">
        <div>
          <label className="block text-sm font-semibold mb-1">Rider Name</label>
          <input type="text" disabled value="Ramesh Sharma" className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-neutral-50" />
        </div>
        <div>
          <label className="block text-sm font-semibold mb-1">Vehicle Details</label>
          <input type="text" disabled value="Hero Splendor (UP16-CZ-1234)" className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-neutral-50" />
        </div>
      </div>
    </div>
  );
};
