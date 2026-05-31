-- ================================================================
-- V1: Schema inicial del Sistema de Inventario y Activos UTS
-- Autor: Sistema de Inventario TI - UTS
-- ================================================================

-- ----- ROLES Y USUARIOS -----

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    document_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- ----- ÁREAS / DEPENDENCIAS -----

CREATE TABLE IF NOT EXISTS areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    location VARCHAR(150),
    floor VARCHAR(20),
    building VARCHAR(50),
    responsible_id BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ----- TIPOS DE ACTIVOS -----

CREATE TABLE IF NOT EXISTS asset_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL,   -- COMPUTING, NETWORK, PERIPHERAL, COMMUNICATION, OTHER
    created_at TIMESTAMP DEFAULT NOW()
);

-- ----- ACTIVOS TECNOLÓGICOS -----

CREATE TABLE IF NOT EXISTS assets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(100),
    serial_number VARCHAR(100) UNIQUE,
    asset_tag VARCHAR(50) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, MAINTENANCE, RETIRED, LOST
    purchase_date DATE,
    purchase_price DECIMAL(14,2),
    warranty_expiry DATE,
    specifications TEXT,
    notes TEXT,
    image_url VARCHAR(500),
    asset_type_id BIGINT REFERENCES asset_types(id),
    area_id BIGINT REFERENCES areas(id),
    assigned_user_id BIGINT REFERENCES users(id),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ----- MOVIMIENTOS DE INVENTARIO -----

CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    movement_type VARCHAR(30) NOT NULL,  -- ENTRY, EXIT, TRANSFER, LOAN, RETURN, MAINTENANCE_IN, MAINTENANCE_OUT
    movement_date TIMESTAMP NOT NULL DEFAULT NOW(),
    from_area_id BIGINT REFERENCES areas(id),
    to_area_id BIGINT REFERENCES areas(id),
    from_user_id BIGINT REFERENCES users(id),
    to_user_id BIGINT REFERENCES users(id),
    reason VARCHAR(255),
    notes TEXT,
    reference_number VARCHAR(50),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ----- DISPOSITIVOS DE RED -----

CREATE TABLE IF NOT EXISTS network_devices (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT UNIQUE REFERENCES assets(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),          -- IPv4 o IPv6
    mac_address VARCHAR(17),         -- formato XX:XX:XX:XX:XX:XX
    hostname VARCHAR(100),
    subnet_mask VARCHAR(15),
    gateway VARCHAR(45),
    dns_primary VARCHAR(45),
    dns_secondary VARCHAR(45),
    vlan_id INTEGER,
    port_number INTEGER,
    is_dhcp BOOLEAN DEFAULT FALSE,
    network_status VARCHAR(20) DEFAULT 'UNKNOWN',  -- ONLINE, OFFLINE, UNKNOWN
    last_seen TIMESTAMP,
    location_detail VARCHAR(255),
    firmware_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ----- TOPOLOGÍA DE RED -----

CREATE TABLE IF NOT EXISTS network_topology (
    id BIGSERIAL PRIMARY KEY,
    source_device_id BIGINT NOT NULL REFERENCES network_devices(id) ON DELETE CASCADE,
    target_device_id BIGINT NOT NULL REFERENCES network_devices(id) ON DELETE CASCADE,
    connection_type VARCHAR(30) NOT NULL,  -- ETHERNET, WIFI, FIBER, SERIAL
    port_source VARCHAR(20),
    port_target VARCHAR(20),
    bandwidth VARCHAR(20),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_topology UNIQUE (source_device_id, target_device_id)
);

-- ----- AUDITORÍA -----

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,        -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW, EXPORT
    entity_type VARCHAR(50),
    entity_id BIGINT,
    entity_description VARCHAR(255),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN DEFAULT TRUE,
    error_message VARCHAR(500),
    timestamp TIMESTAMP DEFAULT NOW()
);

-- ----- ÍNDICES PARA PERFORMANCE -----

CREATE INDEX IF NOT EXISTS idx_assets_status ON assets(status);
CREATE INDEX IF NOT EXISTS idx_assets_area ON assets(area_id);
CREATE INDEX IF NOT EXISTS idx_assets_assigned_user ON assets(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_assets_type ON assets(asset_type_id);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_asset ON inventory_movements(asset_id);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_date ON inventory_movements(movement_date);
CREATE INDEX IF NOT EXISTS idx_network_devices_ip ON network_devices(ip_address);
CREATE INDEX IF NOT EXISTS idx_network_devices_status ON network_devices(network_status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
