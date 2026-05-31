import { useState, useRef } from 'react';
import {
  Box, Typography, Paper, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, FormControl,
  InputLabel, Select, MenuItem, Chip, CircularProgress,
  Alert, Divider, Grid, InputAdornment,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useForm, Controller } from 'react-hook-form';
import { inventoryService } from '../../services/inventoryService';
import { assetService } from '../../services/assetService';
import { useAuth } from '../../context/AuthContext';
import dayjs from 'dayjs';

const MOVEMENT_TYPES = [
  { value: 'ENTRY',           label: 'Entrada' },
  { value: 'EXIT',            label: 'Salida' },
  { value: 'TRANSFER',        label: 'Traslado' },
  { value: 'LOAN',            label: 'Préstamo' },
  { value: 'RETURN',          label: 'Devolución' },
  { value: 'MAINTENANCE_IN',  label: 'Entrada a Mantenimiento' },
  { value: 'MAINTENANCE_OUT', label: 'Salida de Mantenimiento' },
];

const TYPE_COLORS = {
  ENTRY: 'success', EXIT: 'error', TRANSFER: 'info',
  LOAN: 'warning', RETURN: 'default',
  MAINTENANCE_IN: 'warning', MAINTENANCE_OUT: 'success',
};

const STATUS_LABELS = {
  ACTIVE: 'Activo', MAINTENANCE: 'En Mantenimiento',
  RETIRED: 'Dado de Baja', LOST: 'Perdido',
};

export default function InventoryPage() {
  const [open, setOpen]       = useState(false);
  const [page, setPage]       = useState(0);
  const [codigoInput, setCodigoInput] = useState('');
  const [assetFound, setAssetFound]   = useState(null);
  const [assetError, setAssetError]   = useState('');
  const [searching, setSearching]     = useState(false);
  const debounceRef = useRef(null);

  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();

  const { data, isLoading } = useQuery({
    queryKey: ['movements', page],
    queryFn: () => inventoryService.getMovements({ page, size: 15 }),
  });

  const { control, handleSubmit, reset, setValue, formState: { errors } } = useForm({
    defaultValues: { movementType: '', movementDate: '', notes: '', toAreaId: '', assetTypeId: '' },
  });

  const { data: areas = [] } = useQuery({
    queryKey: ['areas'],
    queryFn: assetService.getAreas,
    enabled: open,
  });

  const { data: assetTypes = [] } = useQuery({
    queryKey: ['assetTypes'],
    queryFn: assetService.getTypes,
    enabled: open,
  });

  const handleCodigoChange = (value) => {
    setCodigoInput(value);
    setAssetFound(null);
    setAssetError('');
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!value.trim()) return;
    debounceRef.current = setTimeout(() => buscarActivo(value.trim()), 500);
  };

  const buscarActivo = async (codigo) => {
    setSearching(true);
    try {
      const asset = await assetService.getByCode(codigo);
      setAssetFound(asset);
      setAssetError('');
      setValue('assetTypeId', asset.assetTypeId || '');
      setValue('toAreaId',    asset.areaId      || '');
    } catch {
      setAssetFound(null);
      setAssetError('Activo no válido');
    } finally {
      setSearching(false);
    }
  };

  const handleClose = () => {
    setOpen(false);
    setCodigoInput('');
    setAssetFound(null);
    setAssetError('');
    reset();
  };

  const mutation = useMutation({
    mutationFn: (data) =>
      inventoryService.register({
        assetId:      assetFound.id,
        movementType: data.movementType,
        movementDate: data.movementDate  || null,
        toAreaId:     data.toAreaId      || null,
        assetTypeId:  data.assetTypeId   || null,
        notes:        data.notes         || null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['movements'] });
      queryClient.invalidateQueries({ queryKey: ['assets'] });
      enqueueSnackbar('Movimiento registrado exitosamente', { variant: 'success' });
      handleClose();
    },
    onError: (err) =>
      enqueueSnackbar(err?.response?.data?.error || 'Error al registrar', { variant: 'error' }),
  });

  const onSubmit = (data) => {
    if (!assetFound) {
      enqueueSnackbar('Debe ingresar un código de activo válido', { variant: 'warning' });
      return;
    }
    mutation.mutate(data);
  };

  const columns = [
    { field: 'id', headerName: '#', width: 60 },
    {
      field: 'asset', headerName: 'Activo', flex: 1,
      valueGetter: ({ row }) => row.asset?.name || '—',
    },
    {
      field: 'codigo', headerName: 'Código', width: 120,
      valueGetter: ({ row }) => row.asset?.codigo || '—',
    },
    {
      field: 'movementType', headerName: 'Tipo de Movimiento', width: 200,
      renderCell: ({ value }) => {
        const label = MOVEMENT_TYPES.find((t) => t.value === value)?.label || value;
        return <Chip label={label} color={TYPE_COLORS[value]} size="small" />;
      },
    },
    {
      field: 'movementDate', headerName: 'Fecha', width: 170,
      valueFormatter: ({ value }) =>
        value ? dayjs(value).format('DD/MM/YYYY HH:mm') : '—',
    },
    {
      field: 'notes', headerName: 'Observación', flex: 1,
      valueGetter: ({ row }) => row.notes || '—',
    },
    {
      field: 'createdBy', headerName: 'Registrado por', width: 160,
      valueGetter: ({ row }) => row.createdBy?.fullName || '—',
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Control de Inventario</Typography>
        {hasRole(['ROLE_ADMIN', 'ROLE_TECNICO']) && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>
            Registrar Movimiento
          </Button>
        )}
      </Box>

      <Paper>
        <DataGrid
          rows={data?.content || []}
          columns={columns}
          rowCount={data?.totalElements || 0}
          loading={isLoading}
          paginationMode="server"
          paginationModel={{ page, pageSize: 15 }}
          onPaginationModelChange={(m) => setPage(m.page)}
          pageSizeOptions={[15]}
          disableRowSelectionOnClick
          autoHeight
          sx={{ border: 0 }}
        />
      </Paper>

      {/* DIALOG DE REGISTRO */}
      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar Movimiento de Inventario</DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={2.5} pt={1}>

              {/* BÚSQUEDA POR CÓDIGO */}
              <Typography variant="subtitle2" color="text.secondary">
                Buscar activo por código
              </Typography>
              <TextField
                label="Código del activo"
                value={codigoInput}
                onChange={(e) => handleCodigoChange(e.target.value)}
                fullWidth
                autoFocus
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      {searching
                        ? <CircularProgress size={20} />
                        : assetFound
                          ? <CheckCircleIcon color="success" />
                          : <SearchIcon color="disabled" />}
                    </InputAdornment>
                  ),
                }}
              />

              {assetError && (
                <Alert severity="error" sx={{ py: 0.5 }}>{assetError}</Alert>
              )}

              {/* DATOS DEL ACTIVO (readonly) */}
              {assetFound && (
                <>
                  <Divider />
                  <Typography variant="subtitle2" color="text.secondary">
                    Información del activo
                  </Typography>
                  <Grid container spacing={2}>
                    {/* Nombre — solo lectura */}
                    <Grid item xs={12} sm={6}>
                      <TextField
                        label="Nombre"
                        value={assetFound.name}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        variant="filled"
                        size="small"
                      />
                    </Grid>

                    {/* Tipo de activo — editable, igual que en crear activo */}
                    <Grid item xs={12} sm={6}>
                      <Controller
                        name="assetTypeId"
                        control={control}
                        render={({ field }) => (
                          <FormControl fullWidth size="small">
                            <InputLabel>Tipo de activo</InputLabel>
                            <Select {...field} label="Tipo de activo">
                              {assetTypes.map((t) => (
                                <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
                              ))}
                            </Select>
                          </FormControl>
                        )}
                      />
                    </Grid>

                    {/* Estado actual — solo lectura */}
                    <Grid item xs={12} sm={6}>
                      <TextField
                        label="Estado actual"
                        value={STATUS_LABELS[assetFound.status] || assetFound.status}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        variant="filled"
                        size="small"
                      />
                    </Grid>

                    {/* Área — editable, igual que en crear activo */}
                    <Grid item xs={12} sm={6}>
                      <Controller
                        name="toAreaId"
                        control={control}
                        render={({ field }) => (
                          <FormControl fullWidth size="small">
                            <InputLabel>Área</InputLabel>
                            <Select {...field} label="Área">
                              <MenuItem value="">Sin asignar</MenuItem>
                              {areas.map((a) => (
                                <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                              ))}
                            </Select>
                          </FormControl>
                        )}
                      />
                    </Grid>
                  </Grid>
                  <Divider />

                  {/* CAMPOS DEL MOVIMIENTO */}
                  <Typography variant="subtitle2" color="text.secondary">
                    Datos del movimiento
                  </Typography>
                  <Controller
                    name="movementType"
                    control={control}
                    rules={{ required: 'Seleccione el tipo de movimiento' }}
                    render={({ field }) => (
                      <FormControl fullWidth error={!!errors.movementType}>
                        <InputLabel>Tipo de movimiento *</InputLabel>
                        <Select {...field} label="Tipo de movimiento *">
                          {MOVEMENT_TYPES.map((t) => (
                            <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
                          ))}
                        </Select>
                        {errors.movementType && (
                          <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                            {errors.movementType.message}
                          </Typography>
                        )}
                      </FormControl>
                    )}
                  />
                  <Controller
                    name="movementDate"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Fecha y hora"
                        type="datetime-local"
                        fullWidth
                        InputLabelProps={{ shrink: true }}
                        helperText="Dejar vacío para usar la fecha actual"
                      />
                    )}
                  />
                  <Controller
                    name="notes"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Observación"
                        fullWidth
                        multiline
                        rows={3}
                        placeholder="Descripción del movimiento..."
                      />
                    )}
                  />
                </>
              )}
            </Box>
          </DialogContent>

          <DialogActions sx={{ p: 2 }}>
            <Button onClick={handleClose} disabled={mutation.isPending}>Cancelar</Button>
            <Button
              type="submit"
              variant="contained"
              disabled={mutation.isPending || !assetFound}
            >
              {mutation.isPending ? <CircularProgress size={20} /> : 'Registrar'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
