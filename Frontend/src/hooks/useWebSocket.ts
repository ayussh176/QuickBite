import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client, type StompSubscription } from "@stomp/stompjs";

let stompClient: Client | null = null;
let connectionCount = 0;
const subscribers = new Set<(client: Client) => void>();

export const useWebSocket = (topic: string | null, onMessageReceived: (message: any) => void) => {
  const [connected, setConnected] = useState(false);
  const subscriptionRef = useRef<StompSubscription | null>(null);

  useEffect(() => {
    const token = localStorage.getItem("token") || "";

    if (!stompClient) {
      stompClient = new Client({
        brokerURL: "ws://localhost:8080/api/ws",
        webSocketFactory: () => new SockJS("http://localhost:8080/api/ws"),
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => {
          console.log("[WebSocket Debug]:", str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      stompClient.onConnect = () => {
        console.log("WebSocket connected!");
        setConnected(true);
        subscribers.forEach((sub) => sub(stompClient!));
      };

      stompClient.onDisconnect = () => {
        console.log("WebSocket disconnected.");
        setConnected(false);
      };

      stompClient.onStompError = (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
      };

      stompClient.activate();
    } else if (stompClient.connected) {
      setConnected(true);
    }

    connectionCount++;

    const subscribeToTopic = (client: Client) => {
      if (!topic) return;

      console.log(`Subscribing to topic: ${topic}`);
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }

      subscriptionRef.current = client.subscribe(topic, (message) => {
        try {
          const body = JSON.parse(message.body);
          onMessageReceived(body);
        } catch (err) {
          console.error("Failed to parse message body:", err);
          onMessageReceived(message.body);
        }
      });
    };

    if (stompClient.connected) {
      subscribeToTopic(stompClient);
    } else {
      subscribers.add(subscribeToTopic);
    }

    return () => {
      connectionCount--;
      if (subscriptionRef.current) {
        console.log(`Unsubscribing from topic: ${topic}`);
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
      }
      subscribers.delete(subscribeToTopic);

      if (connectionCount === 0 && stompClient) {
        console.log("Deactivating WebSocket client as connection count reached 0");
        stompClient.deactivate();
        stompClient = null;
      }
    };
  }, [topic, onMessageReceived]);

  return { connected };
};
export default useWebSocket;
