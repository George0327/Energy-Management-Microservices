import { v4 as uuidv4 } from "uuid";
import { useEffect, useState } from "react";
import {
  getAllUsers,
  createUser,
  updateUser,
  deleteUser,
} from "../api/userApi";
import {
  registerUser,
  updateAuthUser,
  deleteAuthUser,
} from "../api/authApi";
import {
  getAllDevices,
  createDevice,
  updateDevice,
  deleteDevice,
} from "../api/deviceApi";
import Header from "../components/Header";
import AdminSupportChat from "../components/AdminSupportChat";
import api from "../api/axiosConfig";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newUser, setNewUser] = useState({
    username: "",
    password: "",
    email: "",
    role: "USER",
  });
  const [newDevice, setNewDevice] = useState({
    name: "",
    maxConsValue: "",
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const usersRes = await getAllUsers();
      const devicesRes = await getAllDevices();
      setUsers(usersRes.data);
      setDevices(devicesRes.data);
    } catch (err) {
      console.error("Failed to fetch data:", err);
    } finally {
      setLoading(false);
    }
  };

  // DEVICE ASSIGNMENT 
  const handleAssignDevice = async (deviceId, userId) => {
    try {
      if (userId === "NONE") {
        await api.put(`/api/devices/${deviceId}/unassign`);
      } else {
        await api.put(`/api/devices/${deviceId}/assign/${userId}`);
      }
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to update device assignment.");
    }
  };

  // USER
  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      const userId = uuidv4();

      await registerUser(userId, newUser.username, newUser.password, newUser.role);
      await createUser({
        id: userId,
        username: newUser.username,
        email: newUser.email,
        role: newUser.role,
      });

      alert("User created successfully!");
      setNewUser({ username: "", password: "", email: "", role: "USER" });
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Error creating user.");
    }
  };

  const handleUpdateUser = async (id) => {
    const updatedUsername = prompt("Enter new username (leave blank to keep current):");
    const updatedEmail = prompt("Enter new email (leave blank to keep current):");
    const updatedPassword = prompt("Enter new password (leave blank to keep current):");
    const updatedRole = prompt("Enter new role (USER or ADMIN, leave blank to keep current):");

    try {
      await updateUser(id, {
        username: updatedUsername || undefined,
        email: updatedEmail || undefined,
        role: updatedRole || undefined,
      });

      await updateAuthUser(id, {
        username: updatedUsername || undefined,
        password: updatedPassword || undefined,
        role: updatedRole || undefined,
      });

      alert("User updated successfully!");
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to update user.");
    }
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;
    try {
      await deleteUser(id);
      await deleteAuthUser(id);
      alert("User deleted successfully!");
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to delete user.");
    }
  };

  // DEVICE
  const handleCreateDevice = async (e) => {
    e.preventDefault();
    try {
      const deviceId = uuidv4();
      await createDevice({
        id: deviceId,
        name: newDevice.name,
        maxConsValue: newDevice.maxConsValue,
        userId: null,
      });
      setNewDevice({ name: "", maxConsValue: "" });
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Error creating device.");
    }
  };

  const handleUpdateDevice = async (id) => {
    const updatedName = prompt("Enter new device name (leave blank to keep current):");
    const updatedMax = prompt("Enter new max consumption (leave blank to keep current):");

    try {
      await updateDevice(id, {
        name: updatedName || undefined,
        maxConsValue: updatedMax || undefined,
      });

      alert("Device updated successfully!");
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to update device.");
    }
  };

  const handleDeleteDevice = async (id) => {
    if (!window.confirm("Are you sure you want to delete this device?")) return;
    try {
      await deleteDevice(id);
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to delete device.");
    }
  };

  const getDevicesByUser = (userId) => devices.filter((d) => d.userId === userId);

  return (
    <>
      <Header />
      <div className="admin-container">
        <div className="admin-content">
          <div className="section-header">
            <h2>🛠️ Admin Dashboard</h2>
            <p className="section-description">
              Manage users and devices
            </p>
          </div>

          {loading ? (
            <div className="loading-spinner">
              <div className="spinner"></div>
              <p>Loading data...</p>
            </div>
          ) : (
            <div className="admin-grid">
              {/* DEVICES SECTION */}
              <section className="admin-card">
                <div className="card-header">
                  <h3>📱 Manage Devices</h3>
                </div>
                <div className="card-body">
                  <form onSubmit={handleCreateDevice} className="admin-form">
                    <div className="form-group-inline">
                      <input
                        type="text"
                        placeholder="Device name"
                        value={newDevice.name}
                        onChange={(e) => setNewDevice({ ...newDevice, name: e.target.value })}
                        required
                      />
                      <input
                        type="number"
                        placeholder="Max consumption"
                        value={newDevice.maxConsValue}
                        onChange={(e) =>
                          setNewDevice({ ...newDevice, maxConsValue: e.target.value })
                        }
                        required
                      />
                      <button type="submit" className="btn-primary">Add</button>
                    </div>
                  </form>

                  <div className="items-list">
                    {devices.map((d) => (
                      <div key={d.id} className="list-item">
                        <div className="item-info">
                          <strong>{d.name}</strong>
                          <span className="item-meta">Max: {d.maxConsValue} kW</span>
                        </div>
                        <div className="item-controls">
                          <select
                            value={d.userId || "NONE"}
                            onChange={(e) => handleAssignDevice(d.id, e.target.value)}
                            className="assign-select"
                          >
                            <option value="NONE">Unassigned</option>
                            {users.map((u) => (
                              <option key={u.id} value={u.id}>
                                {u.username}
                              </option>
                            ))}
                          </select>
                          <button onClick={() => handleUpdateDevice(d.id)} className="btn-sm">Edit</button>
                          <button onClick={() => handleDeleteDevice(d.id)} className="btn-sm btn-danger">Delete</button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </section>

              {/* USERS SECTION */}
              <section className="admin-card">
                <div className="card-header">
                  <h3>👥 Manage Users</h3>
                </div>
                <div className="card-body">
                  <form onSubmit={handleCreateUser} className="admin-form">
                    <div className="form-group-stack">
                      <input
                        type="text"
                        placeholder="Username"
                        value={newUser.username}
                        onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                        required
                      />
                      <input
                        type="password"
                        placeholder="Password"
                        value={newUser.password}
                        onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                        required
                      />
                      <input
                        type="email"
                        placeholder="Email"
                        value={newUser.email}
                        onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                        required
                      />
                      <select
                        value={newUser.role}
                        onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                      >
                        <option value="USER">User</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                      <button type="submit" className="btn-primary">Add User</button>
                    </div>
                  </form>

                  <div className="items-list">
                    {users.map((u) => {
                      const userDevices = getDevicesByUser(u.id);
                      return (
                        <div key={u.id} className="list-item">
                          <div className="item-info">
                            <strong>{u.username}</strong>
                            <span className="item-meta">{u.email}</span>
                          </div>
                          <div className="item-controls">
                            <button onClick={() => handleUpdateUser(u.id)} className="btn-sm">Edit</button>
                            <button onClick={() => handleDeleteUser(u.id)} className="btn-sm btn-danger">Delete</button>
                          </div>
                          {userDevices.length > 0 && (
                            <div className="user-devices">
                              <strong>Devices:</strong>
                              {userDevices.map((d) => (
                                <span key={d.id} className="device-badge">{d.name}</span>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              </section>
            </div>
          )}

          {/* SUPPORT CHAT SECTION */}
          <section className="admin-card full-width">
            <div className="card-header">
              <h3>💬 Customer Support</h3>
            </div>
            <div className="card-body">
              <AdminSupportChat />
            </div>
          </section>
        </div>
      </div>
    </>
  );
}
