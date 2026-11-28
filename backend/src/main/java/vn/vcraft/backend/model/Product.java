package vn.vcraft.backend.model;

import jakarta.persistence.*;

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
    private java.math.BigDecimal referencePrice;
    private Boolean priceVisible;
    private Boolean hidden;
    private Boolean featured;
    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    public Product() {}
    public void setSlug(String v) { slug = v; }
    public void setWorkshop(Workshop v) { workshop = v; }
    public void setCategory(Category v) { category = v; }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public Workshop getWorkshop() { return workshop; }
    public Category getCategory() { return category; }
    public java.math.BigDecimal getReferencePrice() { return referencePrice; }
    public Boolean getPriceVisible() { return priceVisible; }
    public Boolean getHidden()       { return hidden; }
    public Boolean getFeatured()     { return featured; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public java.time.Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setName(String v)                      { name = v; }
    public void setDescription(String v)               { description = v; }
    public void setImagePath(String v)                 { imagePath = v; }
    public void setReferencePrice(java.math.BigDecimal v){ referencePrice = v; }
    public void setPriceVisible(Boolean v)             { priceVisible = v; }
    public void setHidden(Boolean v)                   { hidden = v; }
    public void setFeatured(Boolean v)                 { featured = v; }
    public void setCreatedAt(java.time.Instant v)      { createdAt = v; }
    public void setUpdatedAt(java.time.Instant v)      { updatedAt = v; }
}
