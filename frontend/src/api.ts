import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api/v1";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      console.error("서버와의 통신 에러");
      return Promise.reject(error);
    }

    const status = error.response.status;
    const { success, code, message } = error.response.data;

    // 상태 체크
    if (status === 500) {
    }

    return Promise.reject(error);
  }
);

export default api;
