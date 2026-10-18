import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect(token, userId, onAlertReceived) {
    if (this.connected) {
      console.log("WebSocket already connected");
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
        console.log("WebSocket connected");
        this.connected = true;

  // Register client with JWT token
  this.client.publish({
          destination: "/app/register",
          headers: { Authorization: `Bearer ${token}` },
          body: JSON.stringify({}),
    });

  // Subscribe to user-specific alerts
  const subscription = this.client.subscribe(
          `/topic/alerts/${userId}`,
          (message) => {
            try {
              const alert = JSON.parse(message.body);
              console.log("Received alert:", alert);
              if (onAlertReceived) {
                onAlertReceived(alert);
              }
            } catch (error) {
              console.error("Error parsing alert:", error);
            }
          }
        );

        this.subscriptions.set(userId, subscription);
      };

    this.client.onStompError = (frame) => {
        console.error("WebSocket STOMP error:", frame);
        this.connected = false;
      };

    this.client.onWebSocketError = (error) => {
        console.error("WebSocket connection error:", error);
        this.connected = false;
      };

    this.client.activate();
  }

  disconnect() {
    if (this.client && this.connected) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      this.client.deactivate();
      console.log("WebSocket disconnected");
      this.connected = false;
    }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();
