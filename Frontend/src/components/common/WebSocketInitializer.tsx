import React from "react";
import { useSelector } from "react-redux";
import type { RootState } from "../../redux/store";
import useWebSocket from "../../hooks/useWebSocket";
import toast from "react-hot-toast";

export const WebSocketInitializer: React.FC = () => {
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);

  const notificationTopic = isAuthenticated && user ? `/user/queue/notifications` : null;
  const orderTopic = isAuthenticated && user ? `/user/queue/orders` : null;
  const paymentTopic = isAuthenticated && user ? `/user/queue/payments` : null;

  useWebSocket(notificationTopic, (msg) => {
    console.log("WebSocket notification received:", msg);
    toast(msg.title + ": " + msg.message, { icon: "🔔", duration: 5000 });
    window.dispatchEvent(new CustomEvent("notificationUpdate", { detail: msg }));
  });

  useWebSocket(orderTopic, (msg) => {
    console.log("WebSocket order update received:", msg);
    toast(`Order #${msg.orderNumber} status updated to ${msg.status}!`, { icon: "🍔", duration: 5000 });
    window.dispatchEvent(new CustomEvent("orderUpdate", { detail: msg }));
  });

  useWebSocket(paymentTopic, (msg) => {
    console.log("WebSocket payment update received:", msg);
    toast(`Payment for Order #${msg.orderNumber} is ${msg.status}!`, { icon: "💳", duration: 5000 });
    window.dispatchEvent(new CustomEvent("paymentUpdate", { detail: msg }));
  });

  return null;
};

export default WebSocketInitializer;
