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

    protected Category() {}

    public String name() {
        return name;
    }
}
