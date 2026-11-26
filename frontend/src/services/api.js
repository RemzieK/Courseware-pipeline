import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api/v1",
  timeout: 10000,
});

api.interceptors.request.use(
  (config) => {
    try {
      const stored = localStorage.getItem("unicloud_auth");

      if (stored) {
        const session = JSON.parse(stored);

        if (
          session.token &&
          new Date(session.expiresAt).getTime() > Date.now()
        ) {
          config.headers.Authorization = `Bearer ${session.token}`;
        } else {
          localStorage.removeItem("unicloud_auth");
          window.dispatchEvent(new Event("auth:unauthorized"));
        }
      }
    } catch {
      localStorage.removeItem("unicloud_auth");
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new Event("auth:unauthorized"));
    }

    if (error.response?.status === 403) {
      console.error("Нямате права за тази операция.");
    }

    return Promise.reject(error);
  }
);

export default api;