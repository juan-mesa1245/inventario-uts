import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Button, Grid, TextField, MenuItem, Typography,
  Paper, CircularProgress, FormControl, InputLabel, Select
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { assetService } from '../../services/assetService';
import { DatePicker } from '@mui/x-date-pickers';
import dayjs from 'dayjs';

const schema = yup.object({
  name: yup.string().required('El nombre es obligatorio').max(100),
  brand: yup.string().max(50).optional(),
  model: yup.string().max(100).optional(),
  serialNumber: yup.string().max(100).optional(),
  codigo: yup.string().max(50).optional(),
  status: yup.string().required(),
  assetTypeId: yup.number().required('El tipo de activo es obligatorio').typeError('Seleccione un tipo'),
  areaId: yup.number().optional().nullable(),
  assignedUserId: yup.number().optional().nullable(),
});

export default function AssetFormPage() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  const { data: types } = useQuery({ queryKey: ['assetTypes'], queryFn: assetService.getTypes });
  const { data: areas } = useQuery({ queryKey: ['areas'], queryFn: assetService.getAreas });
  const { data: users } = useQuery({ queryKey: ['users'], queryFn: assetService.getUsers });
  const { data: existing } = useQuery({
    queryKey: ['asset', id],
    queryFn: () => assetService.getById(id),
    enabled: isEditing,
  });

  const { control, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      name: '', brand: '', model: '', serialNumber: '', codigo: '',
      status: 'ACTIVE', assetTypeId: '', areaId: '', assignedUserId: '',
      specifications: '', notes: '',
    },
  });

  useEffect(() => {
    if (existing) {
      reset({
        name: existing.name,
        brand: existing.brand || '',
        model: existing.model || '',
        serialNumber: existing.serialNumber || '',
        codigo: existing.codigo || '',
        status: existing.status,
        assetTypeId: existing.assetTypeId || '',
        areaId: existing.areaId || '',
        assignedUserId: existing.assignedUserId || '',
        specifications: existing.specifications || '',
        notes: existing.notes || '',
        purchaseDate: existing.purchaseDate ? dayjs(existing.purchaseDate) : null,
      });
    }
  }, [existing, reset]);

  const mutation = useMutation({
    mutationFn: (data) => isEditing ? assetService.update(id, data) : assetService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assets'] });
      enqueueSnackbar(isEditing ? 'Activo actualizado' : 'Activo creado', { variant: 'success' });
      navigate('/assets');
    },
    onError: (err) =>
      enqueueSnackbar(err?.response?.data?.error || 'Error al guardar', { variant: 'error' }),
  });

  const onSubmit = (data) => {
    const payload = {
      ...data,
      assetTypeId: Number(data.assetTypeId) || null,
      areaId: Number(data.areaId) || null,
      assignedUserId: Number(data.assignedUserId) || null,
      purchaseDate: data.purchaseDate ? dayjs(data.purchaseDate).format('YYYY-MM-DD') : null,
    };
    mutation.mutate(payload);
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>
        {isEditing ? 'Editar Activo' : 'Nuevo Activo'}
      </Typography>

      <Paper sx={{ p: 3 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Controller name="name" control={control}
                render={({ field }) => (
                  <TextField {...field} fullWidth label="Nombre *" error={!!errors.name}
                    helperText={errors.name?.message} />
                )} />
            </Grid>
            <Grid item xs={12} md={3}>
              <Controller name="brand" control={control}
                render={({ field }) => <TextField {...field} fullWidth label="Marca" />} />
            </Grid>
            <Grid item xs={12} md={3}>
              <Controller name="model" control={control}
                render={({ field }) => <TextField {...field} fullWidth label="Modelo" />} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="serialNumber" control={control}
                render={({ field }) => <TextField {...field} fullWidth label="Número de Serie" />} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="codigo" control={control}
                render={({ field }) => <TextField {...field} fullWidth label="Código" />} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="status" control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Estado *</InputLabel>
                    <Select {...field} label="Estado *">
                      <MenuItem value="ACTIVE">Activo</MenuItem>
                      <MenuItem value="MAINTENANCE">Mantenimiento</MenuItem>
                      <MenuItem value="RETIRED">Dado de Baja</MenuItem>
                      <MenuItem value="LOST">Perdido</MenuItem>
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="assetTypeId" control={control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.assetTypeId}>
                    <InputLabel>Tipo de Activo *</InputLabel>
                    <Select {...field} label="Tipo de Activo *">
                      {types?.map((t) => (
                        <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="areaId" control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Área</InputLabel>
                    <Select {...field} label="Área">
                      <MenuItem value="">Sin asignar</MenuItem>
                      {areas?.map((a) => (
                        <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller name="assignedUserId" control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Usuario Asignado</InputLabel>
                    <Select {...field} label="Usuario Asignado">
                      <MenuItem value="">Sin asignar</MenuItem>
                      {users?.map((u) => (
                        <MenuItem key={u.id} value={u.id}>{u.fullName}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="specifications" control={control}
                render={({ field }) => (
                  <TextField {...field} fullWidth label="Especificaciones técnicas" multiline rows={3} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="notes" control={control}
                render={({ field }) => (
                  <TextField {...field} fullWidth label="Observaciones" multiline rows={2} />
                )} />
            </Grid>
          </Grid>

          <Box display="flex" gap={2} justifyContent="flex-end" mt={3}>
            <Button variant="outlined" onClick={() => navigate('/assets')} disabled={mutation.isPending}>
              Cancelar
            </Button>
            <Button type="submit" variant="contained" disabled={mutation.isPending}>
              {mutation.isPending ? <CircularProgress size={22} /> : (isEditing ? 'Actualizar' : 'Guardar')}
            </Button>
          </Box>
        </form>
      </Paper>
    </Box>
  );
}
