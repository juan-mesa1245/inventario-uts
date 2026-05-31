import {
  AppBar, Toolbar, IconButton, Typography, Box,
  Avatar, Menu, MenuItem, Divider, Chip
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Header({ drawerWidth, onMenuClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleColor = (roles) => {
    if (roles?.includes('ROLE_ADMIN')) return 'error';
    if (roles?.includes('ROLE_TECNICO')) return 'warning';
    return 'default';
  };

  const getRoleLabel = (roles) => {
    if (roles?.includes('ROLE_ADMIN')) return 'Administrador';
    if (roles?.includes('ROLE_TECNICO')) return 'Técnico';
    return 'Usuario';
  };

  return (
    <AppBar
      position="fixed"
      sx={{ zIndex: (theme) => theme.zIndex.drawer + 1, width: '100%' }}
      elevation={1}
    >
      <Toolbar>
        <IconButton color="inherit" edge="start" onClick={onMenuClick} sx={{ mr: 2 }}>
          <MenuIcon />
        </IconButton>

        <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1 }}>
          <Typography variant="h6" noWrap fontWeight={700}>
            Sistema de Inventario TI
          </Typography>
          <Typography variant="caption" sx={{ ml: 1, opacity: 0.8 }}>
            | UTS
          </Typography>
        </Box>

        {user && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip
              label={getRoleLabel(user.roles)}
              color={getRoleColor(user.roles)}
              size="small"
              sx={{ display: { xs: 'none', sm: 'flex' } }}
            />
            <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: 14 }}>
                {user.fullName?.charAt(0).toUpperCase()}
              </Avatar>
            </IconButton>
          </Box>
        )}

        <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
          <Box sx={{ px: 2, py: 1 }}>
            <Typography variant="subtitle2">{user?.fullName}</Typography>
            <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
          </Box>
          <Divider />
          <MenuItem onClick={handleLogout}>
            <LogoutIcon fontSize="small" sx={{ mr: 1 }} />
            Cerrar sesión
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
