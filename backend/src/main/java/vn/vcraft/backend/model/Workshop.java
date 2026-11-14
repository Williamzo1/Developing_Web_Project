package vn.vcraft.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workshops")
public class Workshop {
    @Id
    private Long id;
    private String name;
    private String slug;
    private boolean approved;

    protected Workshop() {}

    public String slug() { return slug; }

    public String name() {
        return name;
    }
}
