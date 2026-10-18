import { useState, useEffect, useRef, useContext } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { sendMessageToAdmin, getMyMessages } from "../api/customerApi";
import { AuthContext } from "../context/AuthContext";
import "./ChatSupport.css";

export default function AdminChat({ onClose }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isConnecting, setIsConnecting] = useState(true);
  const messagesEndRef = useRef(null);
  const stompClientRef = useRef(null);
  const { user, token } = useContext(AuthContext);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Load message history and connect to WebSocket
  useEffect(() => {
    if (!user) return;

    const loadHistory = async () => {
      try {
        const response = await getMyMessages(user.id);
        const history = response.data.map((msg) => ({
          type: msg.senderType === "USER" ? "user" : "admin",
          text: msg.content,
          timestamp: new Date(msg.timestamp),
          messageId: msg.id,
        }));
        
        if (history.length > 0) {
          setMessages(history);
        } else {
          setMessages([
            {
              type: "system",
              text: "Connected to admin support. Please describe your issue.",
              timestamp: new Date(),
            },
          ]);
        }
      } catch (error) {
        console.error("Error loading chat history:", error);
        setMessages([
          {
            type: "system",
            text: "Connected to admin support. Please describe your issue.",
            timestamp: new Date(),
          },
        ]);
      }
    };

    loadHistory();

    // Connect to WebSocket for real-time updates
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log("Chat WebSocket connected");
      setIsConnecting(false);

      // Register with token
      client.publish({
        destination: "/app/register",
        headers: { Authorization: `Bearer ${token}` },
        body: JSON.stringify({}),
      });

      // Subscribe to chat messages for this user
      client.subscribe(`/topic/chat/user/${user.id}`, (message) => {
        try {
          const chatEvent = JSON.parse(message.body);
          console.log("Received chat message:", chatEvent);

          // Only add admin messages (user messages are added on send)
          if (chatEvent.senderType === "ADMIN") {
            setMessages((prev) => [
              ...prev,
              {
                type: "admin",
                text: chatEvent.content,
                timestamp: new Date(chatEvent.timestamp),
                messageId: chatEvent.messageId,
              },
            ]);
          }
        } catch (error) {
          console.error("Error parsing chat message:", error);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error("Chat WebSocket STOMP error:", frame);
      setIsConnecting(false);
    };

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [user, token]);

  const handleSendMessage = async () => {
    if (!input.trim() || !user) return;

    const userMessage = {
      type: "user",
      text: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setIsLoading(true);

    try {
      await sendMessageToAdmin(user.id, input);
      setIsLoading(false);
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
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="chat-support-container">
      <div className="chat-header admin-header">
        <h3>👨‍💼 Admin Support</h3>
        <button className="close-btn" onClick={onClose}>
          ×
        </button>
      </div>

      <div className="chat-messages">
        {isConnecting && (
          <div className="message system">
            <div className="message-bubble">
              <p>Connecting to support...</p>
            </div>
          </div>
        )}
        {messages.map((msg, idx) => (
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
        ))}
        {isLoading && (
          <div className="message system">
            <div className="message-bubble typing">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="chat-input-container">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Message admin..."
          className="chat-input"
          disabled={isLoading}
        />
        <button
          onClick={handleSendMessage}
          className="send-btn"
          disabled={isLoading || !input.trim()}
        >
          Send
        </button>
      </div>
    </div>
  );
}
