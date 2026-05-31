import { useState } from 'react';
import { Box, Typography, Paper, TextField, FormControl, InputLabel, Select, MenuItem, Chip } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { useQuery } from '@tanstack/react-query';
import api from '../../services/api';

const ACTION_COLORS = {
  CREATE: 'success', UPDATE: 'warning', DELETE: 'error',
  LOGIN: 'info', LOGOUT: 'default', VIEW: 'default', EXPORT: 'info'
};

export default function AuditPage() {
  const [filters, setFilters] = useState({ action: '', entityType: '', page: 0 });

  const { data, isLoading } = useQuery({
    queryKey: ['audit', filters],
    queryFn: () => api.get('/audit/logs', {
      params: {
        action: filters.action || undefined,
        entityType: filters.entityType || undefined,
        page: filters.page,
        size: 20,
      }
    }).then((r) => r.data.data),
  });

  const columns = [
    { field: 'id', headerName: '#', width: 60 },
    { field: 'username', headerName: 'Usuario', width: 130 },
    {
      field: 'action', headerName: 'Acción', width: 110,
      renderCell: ({ value }) => <Chip label={value} color={ACTION_COLORS[value] || 'default'} size="small" />
    },
    { field: 'entityType', headerName: 'Entidad', width: 130 },
    { field: 'entityDescription', headerName: 'Descripción', flex: 1 },
    { field: 'ipAddress', headerName: 'IP', width: 130 },
    {
      field: 'success', headerName: 'Éxito', width: 80,
      renderCell: ({ value }) => <Chip label={value ? 'Sí' : 'No'} color={value ? 'success' : 'error'} size="small" />
    },
    {
      field: 'timestamp', headerName: 'Fecha/Hora', width: 170,
      valueFormatter: ({ value }) => value ? new Date(value).toLocaleString('es-CO') : '—'
    },
  ];

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>Registros de Auditoría</Typography>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box display="flex" gap={2} flexWrap="wrap">
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Acción</InputLabel>
            <Select value={filters.action} label="Acción"
              onChange={(e) => setFilters({ ...filters, action: e.target.value, page: 0 })}>
              <MenuItem value="">Todas</MenuItem>
              {['CREATE','UPDATE','DELETE','LOGIN','LOGOUT','VIEW','EXPORT'].map((a) => (
                <MenuItem key={a} value={a}>{a}</MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label="Tipo de Entidad" size="small"
            value={filters.entityType}
            onChange={(e) => setFilters({ ...filters, entityType: e.target.value, page: 0 })}
            placeholder="Asset, User, ..."
          />
        </Box>
      </Paper>
      <Paper>
        <DataGrid
          rows={data?.content || []}
          columns={columns}
          rowCount={data?.totalElements || 0}
          loading={isLoading}
          paginationMode="server"
          paginationModel={{ page: filters.page, pageSize: 20 }}
          onPaginationModelChange={(m) => setFilters({ ...filters, page: m.page })}
          pageSizeOptions={[20]}
          disableRowSelectionOnClick
          autoHeight
          sx={{ border: 0 }}
        />
      </Paper>
    </Box>
  );
}
