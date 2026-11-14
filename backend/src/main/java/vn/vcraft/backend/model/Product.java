package vn.vcraft.backend.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import vn.vcraft.backend.dto.ProductInput;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String slug;
    private String name;
    private String description;
    private String imagePath;
    private BigDecimal referencePrice;
    private boolean priceVisible;
    private boolean hidden;
    private boolean adminHidden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    protected Product() {}

    public Product(Workshop workshop, String slug, Category category, ProductInput input) {
        this.workshop = workshop;
        this.slug = slug;
        update(category, input);
    }

    public void update(Category category, ProductInput input) {
        this.category = category;
        this.name = input.name().strip();
        this.description = input.description();
        this.referencePrice = input.price();
        this.priceVisible = input.priceVisible();
        this.hidden = input.hidden();
    }

    public void hideByAdmin() { this.adminHidden = true; }
    public BigDecimal getReferencePrice() { return referencePrice; }
    public boolean isPriceVisible() { return priceVisible; }
    public boolean isHidden() { return hidden; }
    public boolean isAdminHidden() { return adminHidden; }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public Workshop getWorkshop() { return workshop; }
    public Category getCategory() { return category; }
}
