package com.uts.inventario.dto.request;

import com.uts.inventario.enums.NetworkStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NetworkDeviceRequest {

    @NotNull(message = "El activo asociado es obligatorio")
    private Long assetId;

    @Pattern(
        regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
        message = "Formato de dirección IP inválido"
    )
    private String ipAddress;

    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
        message = "Formato de dirección MAC inválido (XX:XX:XX:XX:XX:XX)"
    )
    private String macAddress;

    @Size(max = 100)
    private String hostname;

    private String subnetMask;

    private String gateway;

    private String dnsPrimary;

    private String dnsSecondary;

    @Min(1) @Max(4094)
    private Integer vlanId;

    @Min(1) @Max(65535)
    private Integer portNumber;

    private Boolean isDhcp;

    private NetworkStatus networkStatus;

    @Size(max = 255)
    private String locationDetail;

    @Size(max = 50)
    private String firmwareVersion;
}
