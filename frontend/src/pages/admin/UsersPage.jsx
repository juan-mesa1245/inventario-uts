import { Box, Typography, Paper, Chip, IconButton, Tooltip, Alert } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import api from '../../services/api';

export default function UsersPage() {
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  const { data: users, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get('/users').then((r) => r.data.data),
  });

  const toggleMutation = useMutation({
    mutationFn: (id) => api.patch(`/users/${id}/toggle-active`),
    onSuccess: () => {
      queryClient.invalidateQueries(['users']);
      enqueueSnackbar('Estado del usuario actualizado', { variant: 'success' });
    },
  });

  const columns = [
    { field: 'id', headerName: 'ID', width: 60 },
    { field: 'fullName', headerName: 'Nombre Completo', flex: 1 },
    { field: 'username', headerName: 'Usuario', width: 140 },
    { field: 'email', headerName: 'Correo', flex: 1 },
    { field: 'phone', headerName: 'Teléfono', width: 130 },
    {
      field: 'roles', headerName: 'Roles', width: 180,
      renderCell: ({ row }) => (
        <Box display="flex" gap={0.5} flexWrap="wrap">
          {row.roles?.map((r) => (
            <Chip key={r.name} label={r.name?.replace('ROLE_', '')} size="small"
              color={r.name === 'ROLE_ADMIN' ? 'error' : r.name === 'ROLE_TECNICO' ? 'warning' : 'default'} />
          ))}
        </Box>
      )
    },
    {
      field: 'isActive', headerName: 'Estado', width: 100,
      renderCell: ({ value }) => (
        <Chip label={value ? 'Activo' : 'Inactivo'} color={value ? 'success' : 'default'} size="small" />
      )
    },
    {
      field: 'actions', headerName: 'Acciones', width: 100, sortable: false,
      renderCell: ({ row }) => (
        <Tooltip title={row.isActive ? 'Desactivar' : 'Activar'}>
          <IconButton size="small" onClick={() => toggleMutation.mutate(row.id)}>
            {row.isActive ? <BlockIcon fontSize="small" color="error" /> : <CheckCircleIcon fontSize="small" color="success" />}
          </IconButton>
        </Tooltip>
      )
    }
  ];

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>Gestión de Usuarios</Typography>
      <Alert severity="info" sx={{ mb: 2 }}>
        Para registrar nuevos usuarios, use el endpoint POST /api/auth/register con rol ADMIN.
      </Alert>
      <Paper>
        <DataGrid
          rows={users || []}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          autoHeight
          sx={{ border: 0 }}
        />
      </Paper>
    </Box>
  );
}
