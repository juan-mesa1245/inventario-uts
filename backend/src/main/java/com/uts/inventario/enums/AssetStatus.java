package com.uts.inventario.enums;

public enum AssetStatus {
    ACTIVE("Activo"),
    MAINTENANCE("En Mantenimiento"),
    RETIRED("Dado de Baja"),
    LOST("Perdido");

    private final String label;

    AssetStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
