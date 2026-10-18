import api from "./axiosConfig";

export const loginUser = async (username, password) => {
  const response = await api.post("/auth/login", { username, password });
  console.log("Login response data:", response.data);
  return response.data;
};

export const registerUser = async (id, username, password, role) => {
  const response = await api.post("/auth/register", { id, username, password, role });
  return response.data;
};

export const updateAuthUser = (id, user) =>{
  console.log("Updating auth user with ID:", id, "Data:", user);
  api.put(`/auth/${id}`, user);
}

export const deleteAuthUser = (id) => api.delete(`/auth/${id}`);
