import api from "./axiosConfig";

export const getAllDevices = () => api.get("/api/devices");
export const createDevice = (device) => api.post("/api/devices", device);
export const updateDevice = (id, device) => api.put(`/api/devices/${id}`, device);
export const deleteDevice = (id) => api.delete(`/api/devices/${id}`);
