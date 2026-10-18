import { useState, useEffect, useRef, useContext } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {
  getActiveUsers,
  getMessagesForUser,
  sendAdminReply,
  getAdminId,
} from "../api/customerApi";
import { AuthContext } from "../context/AuthContext";
import { getAllUsers } from "../api/userApi";
import "./AdminSupportChat.css";

export default function AdminSupportChat() {
  const [activeUsers, setActiveUsers] = useState([]);
  const [userDetails, setUserDetails] = useState({});
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [adminId, setAdminId] = useState(null);
  const messagesEndRef = useRef(null);
  const stompClientRef = useRef(null);
  const { token } = useContext(AuthContext);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Load active users and admin ID
  useEffect(() => {
    const loadData = async () => {
      try {
        const [activeRes, adminRes, allUsersRes] = await Promise.all([
          getActiveUsers(),
          getAdminId(),
          getAllUsers(),
        ]);

        setActiveUsers(activeRes.data);
        setAdminId(adminRes.data);

        // Create a map of userId -> username
        const userMap = {};
        allUsersRes.data.forEach((u) => {
          userMap[u.id] = u.username;
        });
        setUserDetails(userMap);
      } catch (error) {
        console.error("Error loading data:", error);
      }
    };

    loadData();

    // Connect to WebSocket for real-time updates
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log("Admin Chat WebSocket connected");

      // Register with token
      client.publish({
        destination: "/app/register",
        headers: { Authorization: `Bearer ${token}` },
        body: JSON.stringify({}),
      });
    };

    client.onStompError = (frame) => {
      console.error("Admin Chat WebSocket STOMP error:", frame);
    };

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [token]);

  // Subscribe to selected user's chat
  useEffect(() => {
    if (!selectedUserId || !stompClientRef.current?.connected) return;

    const subscription = stompClientRef.current.subscribe(
      `/topic/chat/user/${selectedUserId}`,
      (message) => {
        try {
          const chatEvent = JSON.parse(message.body);
          console.log("Admin received chat message:", chatEvent);

          // Only add user messages (admin messages are added on send)
          if (chatEvent.senderType === "USER") {
            setMessages((prev) => [
              ...prev,
              {
                type: "user",
                text: chatEvent.content,
                timestamp: new Date(chatEvent.timestamp),
                messageId: chatEvent.messageId,
              },
            ]);

            // Add user to active list if not already there
            setActiveUsers((prev) => {
              if (!prev.includes(chatEvent.userId)) {
                return [...prev, chatEvent.userId];
              }
              return prev;
            });
          }
        } catch (error) {
          console.error("Error parsing chat message:", error);
        }
      }
    );

    return () => {
      subscription.unsubscribe();
    };
  }, [selectedUserId]);

  // Load messages when selecting a user
  const handleSelectUser = async (userId) => {
    setSelectedUserId(userId);
    setIsLoading(true);

    try {
      const response = await getMessagesForUser(userId);
      const history = response.data.map((msg) => ({
        type: msg.senderType === "USER" ? "user" : "admin",
        text: msg.content,
        timestamp: new Date(msg.timestamp),
        messageId: msg.id,
      }));
      setMessages(history);
    } catch (error) {
      console.error("Error loading messages:", error);
      setMessages([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSendMessage = async () => {
    if (!input.trim() || !selectedUserId || !adminId) return;

    const adminMessage = {
      type: "admin",
      text: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, adminMessage]);
    setInput("");

    try {
      await sendAdminReply(selectedUserId, adminId, input);
    } catch (error) {
      console.error("Error sending message:", error);
      setMessages((prev) => [
        ...prev,
        {
          type: "system",
          text: "Failed to send message. Please try again.",
          timestamp: new Date(),
        },
      ]);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const getUserName = (userId) => {
    return userDetails[userId] || userId.substring(0, 8) + "...";
  };

  return (
    <div className="admin-support-container">
      {/* Users List */}
      <div className="users-panel">
        <div className="panel-header">
          <h3>💬 Support Chats</h3>
        </div>
        <div className="users-list">
          {activeUsers.length === 0 ? (
            <div className="no-users">No active chats</div>
          ) : (
            activeUsers.map((userId) => (
              <div
                key={userId}
                className={`user-item ${selectedUserId === userId ? "selected" : ""}`}
                onClick={() => handleSelectUser(userId)}
              >
                <div className="user-avatar">👤</div>
                <div className="user-info">
                  <span className="user-name">{getUserName(userId)}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Chat Panel */}
      <div className="chat-panel">
        {!selectedUserId ? (
          <div className="no-chat-selected">
            <p>Select a user to view their chat</p>
          </div>
        ) : (
          <>
            <div className="chat-header">
              <h3>Chat with {getUserName(selectedUserId)}</h3>
            </div>

            <div className="chat-messages">
              {isLoading ? (
                <div className="loading">Loading messages...</div>
              ) : messages.length === 0 ? (
                <div className="no-messages">No messages yet</div>
              ) : (
                messages.map((msg, idx) => (
                  <div key={msg.messageId || idx} className={`message ${msg.type}`}>
                    <div className="message-bubble">
                      <p>{msg.text}</p>
                      <span className="timestamp">
                        {msg.timestamp.toLocaleTimeString([], {
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </span>
                    </div>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>

            <div className="chat-input-container">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Reply to user..."
                className="chat-input"
              />
              <button
                onClick={handleSendMessage}
                className="send-btn"
                disabled={!input.trim()}
              >
                Send
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
