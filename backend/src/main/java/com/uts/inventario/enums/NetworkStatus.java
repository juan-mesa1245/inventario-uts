package com.uts.inventario.enums;

public enum NetworkStatus {
    ONLINE("En línea"),
    OFFLINE("Fuera de línea"),
    UNKNOWN("Desconocido");

    private final String label;

    NetworkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
