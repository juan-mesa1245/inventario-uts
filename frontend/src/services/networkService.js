import api from './api';

export const networkService = {
  getDevices: (params) => api.get('/network/devices', { params }).then((r) => r.data.data),
  getDeviceById: (id) => api.get(`/network/devices/${id}`).then((r) => r.data.data),
  getDeviceByAsset: (assetId) =>
    api.get(`/network/devices/by-asset/${assetId}`).then((r) => r.data.data),
  saveDevice: (data) => api.post('/network/devices', data).then((r) => r.data.data),
  deleteDevice: (id) => api.delete(`/network/devices/${id}`).then((r) => r.data),
  updateStatus: (id, status) =>
    api.patch(`/network/devices/${id}/status`, null, { params: { status } }).then((r) => r.data.data),
  getTopology: () => api.get('/network/topology').then((r) => r.data.data),
  createLink: (sourceId, targetId, connectionType, bandwidth) =>
    api.post('/network/topology', null, {
      params: { sourceId, targetId, connectionType, bandwidth },
    }).then((r) => r.data.data),
  deleteLink: (id) => api.delete(`/network/topology/${id}`).then((r) => r.data),
  getStats: () => api.get('/network/stats').then((r) => r.data.data),
};
