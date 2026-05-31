package com.uts.inventario.entity;

import com.uts.inventario.enums.ConnectionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "network_topology",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"source_device_id", "target_device_id"},
        name = "uq_topology"
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkTopology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_device_id", nullable = false)
    private NetworkDevice sourceDevice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_device_id", nullable = false)
    private NetworkDevice targetDevice;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 30)
    private ConnectionType connectionType;

    @Column(name = "port_source", length = 20)
    private String portSource;

    @Column(name = "port_target", length = 20)
    private String portTarget;

    @Column(length = 20)
    private String bandwidth;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
