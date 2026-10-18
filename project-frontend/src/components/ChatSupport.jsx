import { useState, useEffect, useRef, useContext } from "react";
import { sendChatMessage } from "../api/customerApi";
import { AuthContext } from "../context/AuthContext";
import "./ChatSupport.css";

export default function ChatSupport({ onClose }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const { user } = useContext(AuthContext);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    // Welcome message
    setMessages([
      {
        type: "bot",
        text: "Hello! How can I help you today?",
        timestamp: new Date(),
      },
    ]);
  }, []);

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
      // Send to backend and get response
      const response = await sendChatMessage(user.id, input);
      
      setMessages((prev) => [
        ...prev,
        {
          type: "bot",
          text: response.data,
          timestamp: new Date(),
        },
      ]);
      setIsLoading(false);
    } catch (error) {
      console.error("Error sending message:", error);
      setMessages((prev) => [
        ...prev,
        {
          type: "bot",
          text: "Sorry, there was an error. Please try again.",
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
      <div className="chat-header">
        <h3>🤖 AI Assistant</h3>
        <button className="close-btn" onClick={onClose}>
          ×
        </button>
      </div>

      <div className="chat-messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.type}`}>
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
          <div className="message bot">
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
          placeholder="Ask me anything..."
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
