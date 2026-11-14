package vn.vcraft.backend.model;

import jakarta.persistence.*;
import vn.vcraft.backend.dto.ProcessStepInput;

@Entity
@Table(name = "process_steps")
public class ProcessStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
    private int position;
    private String name;
    private String description;

    protected ProcessStep() {}

    public ProcessStep(Product product, int position, ProcessStepInput input) {
        this.product = product;
        this.position = position;
        this.name = input.name().strip();
        this.description = input.description();
    }

    public ProcessStepInput response() {
        return new ProcessStepInput(name, description);
    }
}
