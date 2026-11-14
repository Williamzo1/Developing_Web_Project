package vn.vcraft.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    private Long id;
    private String name;
    private String slug;

    protected Category() {}

    public String slug() { return slug; }

    public String name() {
        return name;
    }
}
