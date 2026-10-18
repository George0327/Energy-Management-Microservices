import { useEffect, useState, useContext } from "react";
import api from "../api/axiosConfig";
import { AuthContext } from "../context/AuthContext";
import ChatSupport from "../components/ChatSupport";
import AdminChat from "../components/AdminChat";
import Header from "../components/Header";
import WebSocketService from "../services/WebSocketService";
import {
  requestNotificationPermission,
  showConsumptionAlert,
} from "../utils/notificationUtils";
import "./ClientDashboard.css";

export default function ClientDashboard() {
  const [devices, setDevices] = useState([]);
  const [showBotChat, setShowBotChat] = useState(false);
  const [showAdminChat, setShowAdminChat] = useState(false);
  const [loading, setLoading] = useState(true);
  const [alerts, setAlerts] = useState([]);
  const [alertHistory, setAlertHistory] = useState([]);
  const [showFullHistory, setShowFullHistory] = useState(false);
  const { user } = useContext(AuthContext);

  // Load alert history from localStorage
  useEffect(() => {
    if (!user) return;
    const stored = localStorage.getItem(`alerts_${user.id}`);
    if (stored) {
      setAlertHistory(JSON.parse(stored));
    }
  }, [user]);

  useEffect(() => {
    if (!user) return;
    setLoading(true);
    api
      .get(`/api/devices/user/${user.id}`)
      .then((res) => setDevices(res.data))
      .catch((err) => console.error("Failed to load devices:", err))
      .finally(() => setLoading(false));

    // Request notification permission
    requestNotificationPermission();

    // Connect to WebSocket
    const token = localStorage.getItem("token");
    if (token) {
      WebSocketService.connect(token, user.id, handleAlertReceived);
    }

    // Cleanup on unmount
    return () => {
      WebSocketService.disconnect();
    };
  }, [user]);

  const handleAlertReceived = (alert) => {
    console.log("Alert received:", alert);
    
    // Add to current alerts list
    setAlerts((prev) => [alert, ...prev]);

    // Add to persistent history
    const newAlert = {
      ...alert,
      receivedAt: new Date().toISOString(),
      id: `${alert.deviceId}_${Date.now()}`,
    };
    setAlertHistory((prev) => {
      const updated = [newAlert, ...prev];
      // Keep only last 100 alerts
      const trimmed = updated.slice(0, 100);
      localStorage.setItem(`alerts_${user.id}`, JSON.stringify(trimmed));
      return trimmed;
    });

    // Show browser notification
    //showConsumptionAlert(alert);
  };

  const clearAlertHistory = () => {
    setAlertHistory([]);
    localStorage.removeItem(`alerts_${user.id}`);
  };

  const dismissAlert = (index) => {
    setAlerts((prev) => prev.filter((_, i) => i !== index));
  };

  if (!user) return <div className="loading">Loading...</div>;

  return (
    <>
      <Header />
      <div className="dashboard-container">
        <div className="dashboard-content">
          {/* Alert Notifications */}
          {alerts.length > 0 && (
            <div className="alerts-section">
              <h3>⚠️ Recent Alerts</h3>
              {alerts.slice(0, 3).map((alert, index) => (
                <div key={index} className="alert-item">
                  <div className="alert-icon">⚠️</div>
                  <div className="alert-content">
                    <strong>Device {alert.deviceId}</strong> exceeded consumption limit!
                    <div className="alert-details">
                      Consumption: {alert.consumption} kWh | Limit: {alert.maxConsumption} kWh
                    </div>
                    <small>{new Date(alert.timestamp).toLocaleString()}</small>
                  </div>
                  <button
                    className="alert-dismiss"
                    onClick={() => dismissAlert(index)}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Alert History Section */}
          {alertHistory.length > 0 && (
            <div className="alert-history-section">
              <div className="history-header">
                <h3>📋 Alert History</h3>
                <button 
                  className="toggle-history-btn"
                  onClick={() => setShowFullHistory(!showFullHistory)}
                >
                  {showFullHistory ? "Hide" : "Show"} ({alertHistory.length})
                </button>
                <button 
                  className="clear-history-btn"
                  onClick={clearAlertHistory}
                  title="Clear all alert history"
                >
                  🗑️
                </button>
              </div>
              
              {showFullHistory && (
                <div className="alert-history-list">
                  {alertHistory.map((alert) => (
                    <div key={alert.id} className="history-item">
                      <div className="history-timestamp">
                        {new Date(alert.receivedAt).toLocaleString()}
                      </div>
                      <div className="history-content">
                        <strong>Device {alert.deviceId}:</strong> {alert.consumption} kWh (limit: {alert.maxConsumption} kWh)
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="section-header">
            <h2>📱 My Devices</h2>
            <p className="section-description">
              Manage and monitor your connected devices
            </p>
          </div>

          {loading ? (
            <div className="loading-spinner">
              <div className="spinner"></div>
              <p>Loading devices...</p>
            </div>
          ) : devices.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔌</div>
              <h3>No devices yet</h3>
              <p>You don't have any devices assigned to you.</p>
            </div>
          ) : (
            <div className="devices-grid">
              {devices.map((device) => (
                <div key={device.id} className="device-card">
                  <div className="device-card-header">
                    <span className="device-icon">⚡</span>
                    <span className="device-status online">Online</span>
                  </div>
                  <div className="device-card-body">
                    <h3>{device.name}</h3>
                    <div className="device-info">
                      <div className="info-item">
                        <span className="info-label">Max Consumption</span>
                        <span className="info-value">{device.maxConsValue} kW</span>
                      </div>
                      <div className="info-item">
                        <span className="info-label">Device ID</span>
                        <span className="info-value">#{device.id}</span>
                      </div>
                    </div>
                  </div>
                  <div className="device-card-footer">
                    <button className="device-btn">View Details</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Chat Buttons */}
        {!showBotChat && !showAdminChat && (
          <>
            <button
              className="chat-button bot-chat"
              onClick={() => setShowBotChat(true)}
              title="AI Assistant"
            >
              🤖
            </button>
            <button
              className="chat-button admin-chat"
              onClick={() => setShowAdminChat(true)}
              title="Admin Support"
            >
              👨‍💼
            </button>
          </>
        )}

        {/* Chat Windows */}
        {showBotChat && <ChatSupport onClose={() => setShowBotChat(false)} />}
        {showAdminChat && <AdminChat onClose={() => setShowAdminChat(false)} />}
      </div>
    </>
  );
}
