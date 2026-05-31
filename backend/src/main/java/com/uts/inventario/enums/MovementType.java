package com.uts.inventario.enums;

public enum MovementType {
    ENTRY("Entrada"),
    EXIT("Salida"),
    TRANSFER("Traslado"),
    LOAN("Préstamo"),
    RETURN("Devolución"),
    MAINTENANCE_IN("Entrada a Mantenimiento"),
    MAINTENANCE_OUT("Salida de Mantenimiento");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
