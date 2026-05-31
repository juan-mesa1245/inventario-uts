package com.uts.inventario.enums;

public enum ConnectionType {
    ETHERNET("Ethernet"),
    WIFI("Wi-Fi"),
    FIBER("Fibra Óptica"),
    SERIAL("Serial"),
    COAXIAL("Coaxial");

    private final String label;

    ConnectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
