import api from './api';

export const inventoryService = {
  getMovements: (params) => api.get('/inventory/movements', { params }).then((r) => r.data.data),
  getByAsset: (assetId) =>
    api.get(`/inventory/movements/asset/${assetId}`).then((r) => r.data.data),
  getRecent: (limit = 10) =>
    api.get('/inventory/movements/recent', { params: { limit } }).then((r) => r.data.data),
  register: (data) => api.post('/inventory/movements', data).then((r) => r.data.data),
};
