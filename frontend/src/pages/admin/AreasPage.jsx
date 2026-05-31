import { useState } from 'react';
import {
  Box, Typography, Paper, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, IconButton, Tooltip
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useForm, Controller } from 'react-hook-form';
import api from '../../services/api';

export default function AreasPage() {
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  const { data: areas, isLoading } = useQuery({
    queryKey: ['areas'],
    queryFn: () => api.get('/areas').then((r) => r.data.data),
  });

  const { control, handleSubmit, reset, setValue } = useForm({
    defaultValues: { name: '', description: '', location: '', building: '' }
  });

  const mutation = useMutation({
    mutationFn: (data) => editing
      ? api.put(`/areas/${editing.id}`, data)
      : api.post('/areas', data),
    onSuccess: () => {
      queryClient.invalidateQueries(['areas']);
      enqueueSnackbar(editing ? 'Área actualizada' : 'Área creada', { variant: 'success' });
      setOpen(false);
      setEditing(null);
      reset();
    },
    onError: (err) =>
      enqueueSnackbar(err?.response?.data?.error || 'Error al guardar', { variant: 'error' }),
  });

  const handleEdit = (row) => {
    setEditing(row);
    setValue('name', row.name);
    setValue('description', row.description || '');
    setValue('location', row.location || '');
    setValue('building', row.building || '');
    setOpen(true);
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 60 },
    { field: 'name', headerName: 'Nombre', flex: 1 },
    { field: 'description', headerName: 'Descripción', flex: 1 },
    { field: 'location', headerName: 'Ubicación', width: 120 },
    { field: 'building', headerName: 'Edificio', width: 110 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: ({ row }) => (
        <Tooltip title="Editar"><IconButton size="small" onClick={() => handleEdit(row)}>
          <EditIcon fontSize="small" />
        </IconButton></Tooltip>
      )
    }
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Gestión de Áreas</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>
          Nueva Área
        </Button>
      </Box>
      <Paper>
        <DataGrid rows={areas || []} columns={columns} loading={isLoading}
          disableRowSelectionOnClick autoHeight sx={{ border: 0 }} />
      </Paper>

      <Dialog open={open} onClose={() => { setOpen(false); setEditing(null); reset(); }} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar Área' : 'Nueva Área'}</DialogTitle>
        <form onSubmit={handleSubmit((d) => mutation.mutate(d))}>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={2} pt={1}>
              <Controller name="name" control={control} rules={{ required: true }}
                render={({ field }) => <TextField {...field} label="Nombre *" fullWidth />} />
              <Controller name="description" control={control}
                render={({ field }) => <TextField {...field} label="Descripción" fullWidth />} />
              <Controller name="location" control={control}
                render={({ field }) => <TextField {...field} label="Ubicación / Piso" fullWidth />} />
              <Controller name="building" control={control}
                render={({ field }) => <TextField {...field} label="Edificio / Bloque" fullWidth />} />
            </Box>
          </DialogContent>
          <DialogActions sx={{ p: 2 }}>
            <Button onClick={() => { setOpen(false); setEditing(null); reset(); }}>Cancelar</Button>
            <Button type="submit" variant="contained" disabled={mutation.isPending}>
              {editing ? 'Actualizar' : 'Guardar'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
