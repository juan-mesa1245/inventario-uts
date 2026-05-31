import api from './api';

export const assetService = {
  getAll: (params) => api.get('/assets', { params }).then((r) => r.data.data),
  getById: (id) => api.get(`/assets/${id}`).then((r) => r.data.data),
  getByCode: (codigo) => api.get(`/assets/code/${codigo}`).then((r) => r.data.data),
  create: (data) => api.post('/assets', data).then((r) => r.data.data),
  update: (id, data) => api.put(`/assets/${id}`, data).then((r) => r.data.data),
  delete: (id) => api.delete(`/assets/${id}`).then((r) => r.data),
  changeStatus: (id, status) =>
    api.patch(`/assets/${id}/status`, null, { params: { status } }).then((r) => r.data.data),
  getStats: () => api.get('/assets/stats').then((r) => r.data.data),
  getTypes: () => api.get('/asset-types').then((r) => r.data.data),
  getAreas: () => api.get('/areas').then((r) => r.data.data),
  getUsers: () => api.get('/users').then((r) => r.data.data),
};
