import { useState } from 'react';
import {
  Box, Typography, Paper, Chip, TextField, Tab, Tabs,
  Button, IconButton, Tooltip
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import WifiIcon from '@mui/icons-material/Wifi';
import WifiOffIcon from '@mui/icons-material/WifiOff';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { useQuery } from '@tanstack/react-query';
import { networkService } from '../../services/networkService';

const STATUS_COLORS = { ONLINE: 'success', OFFLINE: 'error', UNKNOWN: 'default' };
const STATUS_ICONS = {
  ONLINE: <WifiIcon fontSize="small" />,
  OFFLINE: <WifiOffIcon fontSize="small" />,
  UNKNOWN: <HelpOutlineIcon fontSize="small" />,
};

export default function NetworkPage() {
  const [tab, setTab] = useState(0);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const { data: devices, isLoading } = useQuery({
    queryKey: ['networkDevices', search, page],
    queryFn: () => networkService.getDevices({ search: search || undefined, page, size: 15 }),
  });

  const { data: stats } = useQuery({
    queryKey: ['networkStats'],
    queryFn: networkService.getStats,
  });

  const { data: topology } = useQuery({
    queryKey: ['topology'],
    queryFn: networkService.getTopology,
    enabled: tab === 1,
  });

  const columns = [
    { field: 'hostname', headerName: 'Hostname', flex: 1 },
    { field: 'ipAddress', headerName: 'Dirección IP', width: 150 },
    { field: 'macAddress', headerName: 'MAC', width: 160 },
    {
      field: 'networkStatus',
      headerName: 'Estado',
      width: 130,
      renderCell: ({ value }) => (
        <Chip
          icon={STATUS_ICONS[value]}
          label={value}
          color={STATUS_COLORS[value]}
          size="small"
        />
      ),
    },
    {
      field: 'asset',
      headerName: 'Activo',
      width: 200,
      valueGetter: ({ row }) => row.asset?.name || '—',
    },
    { field: 'vlanId', headerName: 'VLAN', width: 80 },
    { field: 'firmwareVersion', headerName: 'Firmware', width: 120 },
    {
      field: 'lastSeen',
      headerName: 'Último Contacto',
      width: 160,
      valueFormatter: ({ value }) =>
        value ? new Date(value).toLocaleString('es-CO') : '—',
    },
  ];

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={2}>
        Redes & Comunicaciones
      </Typography>

      {/* Summary cards */}
      <Box display="flex" gap={2} mb={3} flexWrap="wrap">
        {[
          { label: 'Total', value: stats?.total, color: 'primary.main' },
          { label: 'Online', value: stats?.online, color: 'success.main' },
          { label: 'Offline', value: stats?.offline, color: 'error.main' },
          { label: 'Desconocido', value: stats?.unknown, color: 'text.secondary' },
        ].map(({ label, value, color }) => (
          <Paper key={label} sx={{ p: 2, minWidth: 120, textAlign: 'center' }}>
            <Typography variant="h5" fontWeight={700} color={color}>{value ?? '—'}</Typography>
            <Typography variant="caption" color="text.secondary">{label}</Typography>
          </Paper>
        ))}
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Dispositivos de Red" />
        <Tab label="Topología" />
      </Tabs>

      {tab === 0 && (
        <Box>
          <TextField
            label="Buscar por IP, hostname o MAC"
            size="small"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            sx={{ mb: 2, minWidth: 300 }}
          />
          <Paper>
            <DataGrid
              rows={devices?.content || []}
              columns={columns}
              rowCount={devices?.totalElements || 0}
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
        </Box>
      )}

      {tab === 1 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="subtitle1" fontWeight={600} mb={2}>
            Topología de Red — {topology?.length || 0} enlaces activos
          </Typography>
          {topology?.length === 0 ? (
            <Typography color="text.secondary">No hay enlaces de topología registrados.</Typography>
          ) : (
            topology?.map((link) => (
              <Box key={link.id} sx={{ borderBottom: '1px solid', borderColor: 'divider', py: 1.5 }}>
                <Typography variant="body2">
                  <strong>{link.sourceDevice?.hostname || link.sourceDevice?.ipAddress}</strong>
                  {' ↔ '}
                  <strong>{link.targetDevice?.hostname || link.targetDevice?.ipAddress}</strong>
                  {' — '}
                  <Chip label={link.connectionType} size="small" />
                  {link.bandwidth && ` | ${link.bandwidth}`}
                </Typography>
              </Box>
            ))
          )}
        </Paper>
      )}
    </Box>
  );
}
