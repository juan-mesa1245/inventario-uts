-- ================================================================
-- V2: Datos iniciales del sistema
-- ================================================================

-- Roles del sistema
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Administrador con acceso total al sistema'),
    ('ROLE_TECNICO', 'Técnico de soporte con acceso a gestión de activos y red'),
    ('ROLE_USUARIO', 'Usuario estándar con acceso de consulta')
ON CONFLICT (name) DO NOTHING;

-- Usuario administrador por defecto (password: Admin@123)
-- Hash BCrypt de 'Admin@123'
INSERT INTO users (username, email, password, full_name, phone, is_active)
VALUES (
    'admin',
    'admin@uts.edu.co',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Administrador del Sistema',
    '6076543210',
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Asignar rol ADMIN al usuario admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Usuario técnico de prueba (password: Tecnico@123)
INSERT INTO users (username, email, password, full_name, phone, is_active)
VALUES (
    'tecnico1',
    'tecnico@uts.edu.co',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Juan Pérez Técnico',
    '3001234567',
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'tecnico1' AND r.name = 'ROLE_TECNICO'
ON CONFLICT DO NOTHING;

-- Tipos de activos
INSERT INTO asset_types (name, description, category) VALUES
    ('Computador de Escritorio', 'PC de escritorio o workstation', 'COMPUTING'),
    ('Portátil', 'Computador portátil / laptop', 'COMPUTING'),
    ('Servidor', 'Servidor físico o blade', 'COMPUTING'),
    ('Router', 'Enrutador de red', 'NETWORK'),
    ('Switch', 'Conmutador de red', 'NETWORK'),
    ('Access Point', 'Punto de acceso inalámbrico', 'NETWORK'),
    ('Firewall', 'Dispositivo de seguridad perimetral', 'NETWORK'),
    ('Impresora', 'Impresora o multifuncional', 'PERIPHERAL'),
    ('Monitor', 'Pantalla o display', 'PERIPHERAL'),
    ('UPS', 'Sistema de alimentación ininterrumpida', 'OTHER'),
    ('Teléfono IP', 'Teléfono VoIP', 'COMMUNICATION'),
    ('Proyector', 'Videoproyector', 'PERIPHERAL'),
    ('Scanner', 'Escáner de documentos', 'PERIPHERAL'),
    ('NAS', 'Almacenamiento en red', 'COMPUTING'),
    ('Tablet', 'Tableta o dispositivo móvil', 'COMPUTING')
ON CONFLICT (name) DO NOTHING;

-- Áreas de la institución
INSERT INTO areas (name, description, location, building) VALUES
    ('Sala de Sistemas 1', 'Laboratorio de informática piso 1', 'Piso 1', 'Bloque A'),
    ('Sala de Sistemas 2', 'Laboratorio de informática piso 2', 'Piso 2', 'Bloque A'),
    ('Oficina de Sistemas', 'Área administrativa de TI', 'Piso 3', 'Bloque B'),
    ('Data Center', 'Centro de datos principal', 'Sótano', 'Bloque B'),
    ('Dirección Académica', 'Oficinas de dirección académica', 'Piso 4', 'Bloque C'),
    ('Rectoría', 'Oficina de rectoría', 'Piso 5', 'Bloque C'),
    ('Biblioteca', 'Biblioteca y sala de consulta', 'Piso 1', 'Bloque D'),
    ('Sala de Profesores', 'Área de profesores y preparación', 'Piso 2', 'Bloque D'),
    ('Bienestar Universitario', 'Área de bienestar estudiantil', 'Piso 1', 'Bloque E'),
    ('Almacén TI', 'Bodega y almacén de equipos', 'Sótano', 'Bloque A')
ON CONFLICT (name) DO NOTHING;
