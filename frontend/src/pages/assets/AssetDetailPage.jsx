import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Paper, Grid, Chip, Button, Divider,
  Tab, Tabs, CircularProgress, Alert, TextField,
  FormControl, InputLabel, Select, MenuItem,
  FormControlLabel, Switch,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { useSnackbar } from 'notistack';
import { assetService } from '../../services/assetService';
import { inventoryService } from '../../services/inventoryService';
import { networkService } from '../../services/networkService';
import { useAuth } from '../../context/AuthContext';
import dayjs from 'dayjs';

const STATUS_COLORS = {
  ACTIVE: 'success', MAINTENANCE: 'warning', RETIRED: 'error', LOST: 'default'
};

const MOVEMENT_LABELS = {
  ENTRY:           'Entrada',
  EXIT:            'Salida',
  TRANSFER:        'Traslado',
  LOAN:            'Préstamo',
  RETURN:          'Devolución',
  MAINTENANCE_IN:  'Entrada a Mantenimiento',
  MAINTENANCE_OUT: 'Salida de Mantenimiento',
};

const MOVEMENT_COLORS = {
  ENTRY: 'success', EXIT: 'error', TRANSFER: 'info',
  LOAN: 'warning', RETURN: 'default',
  MAINTENANCE_IN: 'warning', MAINTENANCE_OUT: 'success',
};

const NETWORK_STATUS_COLORS = { ONLINE: 'success', OFFLINE: 'error', UNKNOWN: 'default' };
const NETWORK_STATUS_LABELS = { ONLINE: 'En línea', OFFLINE: 'Desconectado', UNKNOWN: 'Desconocido' };

const NET_DEFAULTS = {
  ipAddress: '', macAddress: '', hostname: '', subnetMask: '',
  gateway: '', dnsPrimary: '', dnsSecondary: '',
  vlanId: '', portNumber: '', isDhcp: false,
  networkStatus: 'UNKNOWN', firmwareVersion: '', locationDetail: '',
};

function InfoRow({ label, value }) {
  return (
    <Box mb={1.5}>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography variant="body2" fontWeight={500}>{value || '—'}</Typography>
    </Box>
  );
}

/* ── Formulario de configuración de red ── */
function NetworkForm({ assetId, canEdit }) {
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  const { data: netDevice, isLoading } = useQuery({
    queryKey: ['networkDevice', assetId],
    queryFn: () => networkService.getDeviceByAsset(assetId).catch(() => null),
    retry: false,
  });

  const { control, handleSubmit, reset, watch, formState: { errors, isDirty } } = useForm({
    defaultValues: NET_DEFAULTS,
  });

  const isDhcp = watch('isDhcp');

  useEffect(() => {
    if (netDevice) {
      reset({
        ipAddress:      netDevice.ipAddress      ?? '',
        macAddress:     netDevice.macAddress     ?? '',
        hostname:       netDevice.hostname       ?? '',
        subnetMask:     netDevice.subnetMask     ?? '',
        gateway:        netDevice.gateway        ?? '',
        dnsPrimary:     netDevice.dnsPrimary     ?? '',
        dnsSecondary:   netDevice.dnsSecondary   ?? '',
        vlanId:         netDevice.vlanId         ?? '',
        portNumber:     netDevice.portNumber     ?? '',
        isDhcp:         netDevice.isDhcp         ?? false,
        networkStatus:  netDevice.networkStatus  ?? 'UNKNOWN',
        firmwareVersion: netDevice.firmwareVersion ?? '',
        locationDetail: netDevice.locationDetail ?? '',
      });
    } else {
      reset(NET_DEFAULTS);
    }
  }, [netDevice, reset]);

  const mutation = useMutation({
    mutationFn: (data) =>
      networkService.saveDevice({
        assetId:        Number(assetId),
        ipAddress:      data.ipAddress      || null,
        macAddress:     data.macAddress     || null,
        hostname:       data.hostname       || null,
        subnetMask:     data.subnetMask     || null,
        gateway:        data.gateway        || null,
        dnsPrimary:     data.dnsPrimary     || null,
        dnsSecondary:   data.dnsSecondary   || null,
        vlanId:         data.vlanId         ? Number(data.vlanId)     : null,
        portNumber:     data.portNumber     ? Number(data.portNumber) : null,
        isDhcp:         data.isDhcp,
        networkStatus:  data.networkStatus  || 'UNKNOWN',
        firmwareVersion: data.firmwareVersion || null,
        locationDetail: data.locationDetail  || null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['networkDevice', assetId] });
      queryClient.invalidateQueries({ queryKey: ['asset', assetId] });
      queryClient.invalidateQueries({ queryKey: ['networkDevices'] });
      queryClient.invalidateQueries({ queryKey: ['networkStats'] });
      enqueueSnackbar('Configuración de red guardada', { variant: 'success' });
    },
    onError: (err) =>
      enqueueSnackbar(err?.response?.data?.error || 'Error al guardar', { variant: 'error' }),
  });

  if (isLoading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="subtitle1" fontWeight={600}>
          {netDevice ? 'Configuración de Red' : 'Agregar Configuración de Red'}
        </Typography>
        {netDevice && (
          <Chip
            label={NETWORK_STATUS_LABELS[netDevice.networkStatus] ?? netDevice.networkStatus}
            color={NETWORK_STATUS_COLORS[netDevice.networkStatus] ?? 'default'}
            size="small"
          />
        )}
      </Box>

      <form onSubmit={handleSubmit((d) => mutation.mutate(d))}>
        {/* ── Sección 1: Identificación ── */}
        <Typography variant="subtitle2" color="text.secondary" mb={1.5}>
          Identificación
        </Typography>
        <Grid container spacing={2} mb={3}>
          <Grid item xs={12} sm={6} md={4}>
            <Controller name="hostname" control={control}
              render={({ field }) => (
                <TextField {...field} label="Hostname" fullWidth size="small"
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <Controller name="macAddress" control={control}
              rules={{
                pattern: {
                  value: /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/,
                  message: 'Formato: XX:XX:XX:XX:XX:XX',
                },
              }}
              render={({ field }) => (
                <TextField {...field} label="Dirección MAC" fullWidth size="small"
                  placeholder="AA:BB:CC:DD:EE:FF"
                  error={!!errors.macAddress} helperText={errors.macAddress?.message}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <Controller name="firmwareVersion" control={control}
              render={({ field }) => (
                <TextField {...field} label="Versión de Firmware" fullWidth size="small"
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
        </Grid>

        {/* ── Sección 2: Configuración IP ── */}
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={1.5}>
          <Typography variant="subtitle2" color="text.secondary">
            Configuración IP
          </Typography>
          {canEdit && (
            <Controller name="isDhcp" control={control}
              render={({ field }) => (
                <FormControlLabel
                  control={<Switch {...field} checked={field.value} size="small" />}
                  label={<Typography variant="caption">DHCP</Typography>}
                  labelPlacement="start"
                />
              )} />
          )}
        </Box>
        <Grid container spacing={2} mb={3}>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="ipAddress" control={control}
              rules={{
                pattern: {
                  value: /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
                  message: 'IP inválida',
                },
              }}
              render={({ field }) => (
                <TextField {...field} label="Dirección IP" fullWidth size="small"
                  placeholder="192.168.1.100"
                  error={!!errors.ipAddress} helperText={errors.ipAddress?.message}
                  disabled={isDhcp && canEdit}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="subnetMask" control={control}
              render={({ field }) => (
                <TextField {...field} label="Máscara de Subred" fullWidth size="small"
                  placeholder="255.255.255.0"
                  disabled={isDhcp && canEdit}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="gateway" control={control}
              render={({ field }) => (
                <TextField {...field} label="Gateway" fullWidth size="small"
                  placeholder="192.168.1.1"
                  disabled={isDhcp && canEdit}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="vlanId" control={control}
              rules={{ min: { value: 1, message: 'Mín 1' }, max: { value: 4094, message: 'Máx 4094' } }}
              render={({ field }) => (
                <TextField {...field} label="VLAN ID" fullWidth size="small" type="number"
                  inputProps={{ min: 1, max: 4094 }}
                  error={!!errors.vlanId} helperText={errors.vlanId?.message}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="dnsPrimary" control={control}
              render={({ field }) => (
                <TextField {...field} label="DNS Primario" fullWidth size="small"
                  placeholder="8.8.8.8"
                  disabled={isDhcp && canEdit}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="dnsSecondary" control={control}
              render={({ field }) => (
                <TextField {...field} label="DNS Secundario" fullWidth size="small"
                  placeholder="8.8.4.4"
                  disabled={isDhcp && canEdit}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="portNumber" control={control}
              rules={{ min: { value: 1, message: 'Mín 1' }, max: { value: 65535, message: 'Máx 65535' } }}
              render={({ field }) => (
                <TextField {...field} label="Puerto" fullWidth size="small" type="number"
                  inputProps={{ min: 1, max: 65535 }}
                  error={!!errors.portNumber} helperText={errors.portNumber?.message}
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Controller name="networkStatus" control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">
                  <InputLabel>Estado de red</InputLabel>
                  <Select {...field} label="Estado de red"
                    inputProps={{ readOnly: !canEdit }}
                    sx={{ bgcolor: canEdit ? 'transparent' : 'action.hover' }}>
                    <MenuItem value="ONLINE">En línea</MenuItem>
                    <MenuItem value="OFFLINE">Desconectado</MenuItem>
                    <MenuItem value="UNKNOWN">Desconocido</MenuItem>
                  </Select>
                </FormControl>
              )} />
          </Grid>
        </Grid>

        {/* ── Sección 3: Ubicación ── */}
        <Typography variant="subtitle2" color="text.secondary" mb={1.5}>
          Ubicación física
        </Typography>
        <Grid container spacing={2} mb={3}>
          <Grid item xs={12}>
            <Controller name="locationDetail" control={control}
              render={({ field }) => (
                <TextField {...field} label="Detalle de ubicación" fullWidth size="small"
                  placeholder="Ej: Rack 3, piso 2, sala de servidores"
                  InputProps={{ readOnly: !canEdit }} variant={canEdit ? 'outlined' : 'filled'} />
              )} />
          </Grid>
        </Grid>

        {canEdit && (
          <Box display="flex" justifyContent="flex-end">
            <Button
              type="submit"
              variant="contained"
              startIcon={mutation.isPending ? <CircularProgress size={16} /> : <SaveIcon />}
              disabled={mutation.isPending || !isDirty}
            >
              {netDevice ? 'Actualizar configuración' : 'Guardar configuración'}
            </Button>
          </Box>
        )}
      </form>
    </Paper>
  );
}

/* ── Página principal ── */
export default function AssetDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const [tab, setTab] = useState(0);

  const { data: asset, isLoading, error } = useQuery({
    queryKey: ['asset', id],
    queryFn: () => assetService.getById(id),
  });

  const { data: movements } = useQuery({
    queryKey: ['movements', id],
    queryFn: () => inventoryService.getByAsset(id),
    enabled: tab === 1,
  });

  if (isLoading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">Error al cargar el activo</Alert>;

  const canEdit = hasRole(['ROLE_ADMIN', 'ROLE_TECNICO']);

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box display="flex" alignItems="center" gap={1}>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/assets')}>
            Volver
          </Button>
          <Typography variant="h5" fontWeight={700}>{asset?.name}</Typography>
          <Chip label={asset?.statusLabel} color={STATUS_COLORS[asset?.status]} />
        </Box>
        {canEdit && (
          <Button variant="contained" startIcon={<EditIcon />}
            onClick={() => navigate(`/assets/${id}/edit`)}>
            Editar
          </Button>
        )}
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Información General" />
        <Tab label="Historial de Movimientos" />
        <Tab label="Configuración de Red" />
      </Tabs>

      {/* ── Tab 0: Información general ── */}
      {tab === 0 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>Datos del Activo</Typography>
              <InfoRow label="Nombre" value={asset?.name} />
              <InfoRow label="Marca / Modelo" value={`${asset?.brand || ''} ${asset?.model || ''}`.trim()} />
              <InfoRow label="Número de Serie" value={asset?.serialNumber} />
              <InfoRow label="Código" value={asset?.codigo} />
              <InfoRow label="Tipo" value={asset?.assetTypeName} />
              <InfoRow label="Categoría" value={asset?.assetTypeCategory} />
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>Asignación</Typography>
              <InfoRow label="Área" value={asset?.areaName} />
              <InfoRow label="Ubicación" value={asset?.areaLocation} />
              <InfoRow label="Usuario Asignado" value={asset?.assignedUserName} />
              <InfoRow label="Correo" value={asset?.assignedUserEmail} />
            </Paper>
          </Grid>
          {asset?.specifications && (
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="subtitle1" fontWeight={600} mb={1}>Especificaciones</Typography>
                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{asset.specifications}</Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      {/* ── Tab 1: Historial de movimientos ── */}
      {tab === 1 && (
        <Box>
          <Typography variant="subtitle1" fontWeight={600} mb={2}>
            Historial de Movimientos
          </Typography>
          {!movements || movements.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="text.secondary">Sin movimientos registrados</Typography>
            </Paper>
          ) : (
            movements.map((m) => (
              <Paper key={m.id} sx={{ p: 3, mb: 2 }}>
                <Box display="flex" alignItems="center" gap={1.5} mb={2}>
                  <Chip
                    label={MOVEMENT_LABELS[m.movementType] ?? m.movementType}
                    color={MOVEMENT_COLORS[m.movementType] ?? 'default'}
                    size="small"
                  />
                  <Typography variant="body2" color="text.secondary">
                    {dayjs(m.movementDate).format('DD/MM/YYYY HH:mm')}
                  </Typography>
                  {m.referenceNumber && (
                    <Typography variant="body2" color="text.secondary">
                      · Ref: {m.referenceNumber}
                    </Typography>
                  )}
                </Box>

                <Divider sx={{ mb: 2 }} />

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField label="Área origen" value={m.fromArea?.name ?? '—'}
                      size="small" fullWidth variant="filled"
                      InputProps={{ readOnly: true, disableUnderline: true }} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField label="Área destino" value={m.toArea?.name ?? '—'}
                      size="small" fullWidth variant="filled"
                      InputProps={{ readOnly: true, disableUnderline: true }} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField label="Usuario origen" value={m.fromUser?.fullName ?? '—'}
                      size="small" fullWidth variant="filled"
                      InputProps={{ readOnly: true, disableUnderline: true }} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField label="Usuario destino" value={m.toUser?.fullName ?? '—'}
                      size="small" fullWidth variant="filled"
                      InputProps={{ readOnly: true, disableUnderline: true }} />
                  </Grid>
                  {m.reason && (
                    <Grid item xs={12} sm={6}>
                      <TextField label="Razón" value={m.reason}
                        size="small" fullWidth variant="filled"
                        InputProps={{ readOnly: true, disableUnderline: true }} />
                    </Grid>
                  )}
                  {m.notes && (
                    <Grid item xs={12} sm={m.reason ? 6 : 12}>
                      <TextField label="Observaciones" value={m.notes}
                        size="small" fullWidth multiline rows={2} variant="filled"
                        InputProps={{ readOnly: true, disableUnderline: true }} />
                    </Grid>
                  )}
                </Grid>

                {m.createdBy && (
                  <Box mt={1.5}>
                    <Typography variant="caption" color="text.secondary">
                      Registrado por: {m.createdBy.fullName}
                    </Typography>
                  </Box>
                )}
              </Paper>
            ))
          )}
        </Box>
      )}

      {/* ── Tab 2: Configuración de red ── */}
      {tab === 2 && (
        <NetworkForm assetId={id} canEdit={canEdit} />
      )}
    </Box>
  );
}
