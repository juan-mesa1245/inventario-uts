package com.uts.inventario.entity;

import com.uts.inventario.enums.NetworkStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "network_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", unique = true)
    private Asset asset;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(length = 100)
    private String hostname;

    @Column(name = "subnet_mask", length = 15)
    private String subnetMask;

    @Column(length = 45)
    private String gateway;

    @Column(name = "dns_primary", length = 45)
    private String dnsPrimary;

    @Column(name = "dns_secondary", length = 45)
    private String dnsSecondary;

    @Column(name = "vlan_id")
    private Integer vlanId;

    @Column(name = "port_number")
    private Integer portNumber;

    @Column(name = "is_dhcp")
    @Builder.Default
    private Boolean isDhcp = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_status", length = 20)
    @Builder.Default
    private NetworkStatus networkStatus = NetworkStatus.UNKNOWN;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "location_detail", length = 255)
    private String locationDetail;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
