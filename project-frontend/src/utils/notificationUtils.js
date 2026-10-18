export const requestNotificationPermission = async () => {
  if (!("Notification" in window)) {
    console.log("This browser does not support desktop notifications");
    return false;
  }

  if (Notification.permission === "granted") {
    return true;
  }

  if (Notification.permission !== "denied") {
    const permission = await Notification.requestPermission();
    return permission === "granted";
  }

  return false;
};

export const showNotification = (title, options = {}) => {
  if (Notification.permission === "granted") {
    new Notification(title, {
      icon: "/favicon.ico",
      badge: "/favicon.ico",
      ...options,
    });
  }
};

export const showConsumptionAlert = (alert) => {
  const title = "⚠️ Consumption Alert";
  const body = `Device ${alert.deviceId} exceeded limit!\nConsumption: ${alert.consumption} kWh\nLimit: ${alert.maxConsumption} kWh`;

  showNotification(title, {
    body: body,
    requireInteraction: true,
    tag: `alert-${alert.deviceId}`,
  });
};
