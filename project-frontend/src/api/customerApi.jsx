import api from "./axiosConfig";

export const sendChatMessage = (userId, message) =>
  api.post("/support/chat", { userId, message });

export const sendMessageToAdmin = (userId, message) =>
  api.post("/support/messages", { userId, message });

export const sendAdminReply = (userId, senderId, message) =>
  api.post("/support/messages", { userId, senderId, senderType: "ADMIN", message });

export const getMessagesForUser = (userId) =>
  api.get(`/support/messages/user/${userId}`);

export const getMyMessages = (userId) =>
  api.get(`/support/messages/my/${userId}`);

export const getActiveUsers = () =>
  api.get("/support/users/active");

export const getAdminId = () =>
  api.get("/support/admin/id");
