import api from './api';

export const authService = {
  async login(username, password) {
    const { data } = await api.post('/auth/login', { username, password });
    return data.data;
  },

  setToken(token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  },

  clearToken() {
    delete api.defaults.headers.common['Authorization'];
  },
};
