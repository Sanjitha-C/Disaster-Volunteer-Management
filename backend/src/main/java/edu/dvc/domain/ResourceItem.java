package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "resources", indexes = @Index(name = "idx_resource_status", columnList = "status"))
public class ResourceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;
    @Min(0) @Column(nullable = false)
    private int quantity;
    @NotBlank @Size(max = 32)
    @Column(nullable = false, length = 32)
    private String unit;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }
}
