import { Routes, Route, Navigate } from 'react-router-dom';
import { createTheme, ThemeProvider, CssBaseline } from '@mui/material';
import { esES } from '@mui/material/locale';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/layout/MainLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import AssetListPage from './pages/assets/AssetListPage';
import AssetFormPage from './pages/assets/AssetFormPage';
import AssetDetailPage from './pages/assets/AssetDetailPage';
import NetworkPage from './pages/network/NetworkPage';
import InventoryPage from './pages/inventory/InventoryPage';
import UsersPage from './pages/admin/UsersPage';
import AuditPage from './pages/admin/AuditPage';
import AreasPage from './pages/admin/AreasPage';

const theme = createTheme(
  {
    palette: {
      primary: { main: '#1565C0' },
      secondary: { main: '#0288D1' },
      background: { default: '#F5F7FA' },
    },
    typography: {
      fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: { textTransform: 'none', borderRadius: 8 },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: { borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
        },
      },
    },
  },
  esES
);

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="assets" element={<AssetListPage />} />
          <Route path="assets/new" element={<AssetFormPage />} />
          <Route path="assets/:id" element={<AssetDetailPage />} />
          <Route path="assets/:id/edit" element={<AssetFormPage />} />
          <Route path="network" element={<NetworkPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route
            path="admin/users"
            element={
              <ProtectedRoute roles={['ROLE_ADMIN']}>
                <UsersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="admin/areas"
            element={
              <ProtectedRoute roles={['ROLE_ADMIN']}>
                <AreasPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="admin/audit"
            element={
              <ProtectedRoute roles={['ROLE_ADMIN']}>
                <AuditPage />
              </ProtectedRoute>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </ThemeProvider>
  );
}
