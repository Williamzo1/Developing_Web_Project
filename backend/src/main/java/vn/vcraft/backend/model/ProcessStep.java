package vn.vcraft.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "process_steps")
public class ProcessStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Short position;
    private String name;
    private String description;

    public ProcessStep() {}
    // Getters + Setters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Short getPosition() { return position; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setProductId(Long v) { productId = v; }
    public void setPosition(Short v) { position = v; }
    public void setName(String v) { name = v; }
    public void setDescription(String v) { description = v; }
}
