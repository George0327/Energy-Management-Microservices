import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import "./Header.css";

export default function Header() {
  const { user, logout } = useContext(AuthContext);

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-left">
          <h1 className="logo">⚡ Energy Manager</h1>
        </div>
        
        <div className="header-right">
          {user && (
            <>
              <div className="user-info">
                <span className="user-name">{user.name || "User"}</span>
                <span className="user-email">{user.email}</span>
              </div>
              <button className="logout-btn" onClick={handleLogout}>
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
