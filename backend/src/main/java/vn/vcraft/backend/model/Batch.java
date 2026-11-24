package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "batches")
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long workshopId;
    private String code;
    private String publicCode;
    private String name;
    private String description;
    private String kind;
    private Integer quantity;
    private String unit;
    private BigDecimal referencePrice;
    private Boolean priceVisible;
    private String supplyCapacity;
    private LocalDate availableFrom;
    private String visibility;
    private String shareToken;
    private String partnerNote;
    private Boolean hidden;
    private Boolean featured;
    private Instant createdAt;
    private Instant updatedAt;

    public Batch() {}

    // Getters
    public Long getId()               { return id; }
    public Long getProductId()        { return productId; }
    public Long getWorkshopId()       { return workshopId; }
    public String getCode()           { return code; }
    public String getPublicCode()     { return publicCode; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public String getKind()           { return kind; }
    public Integer getQuantity()      { return quantity; }
    public String getUnit()           { return unit; }
    public BigDecimal getReferencePrice(){ return referencePrice; }
    public Boolean getPriceVisible()  { return priceVisible; }
    public String getSupplyCapacity() { return supplyCapacity; }
    public LocalDate getAvailableFrom(){ return availableFrom; }
    public String getVisibility()     { return visibility; }
    public String getShareToken()     { return shareToken; }
    public String getPartnerNote()    { return partnerNote; }
    public Boolean getHidden()        { return hidden; }
    public Boolean getFeatured()      { return featured; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }

    // Setters
    public void setProductId(Long v)        { productId = v; }
    public void setWorkshopId(Long v)       { workshopId = v; }
    public void setCode(String v)           { code = v; }
    public void setPublicCode(String v)     { publicCode = v; }
    public void setName(String v)           { name = v; }
    public void setDescription(String v)    { description = v; }
    public void setKind(String v)           { kind = v; }
    public void setQuantity(Integer v)      { quantity = v; }
    public void setUnit(String v)           { unit = v; }
    public void setReferencePrice(BigDecimal v){ referencePrice = v; }
    public void setPriceVisible(Boolean v)  { priceVisible = v; }
    public void setSupplyCapacity(String v) { supplyCapacity = v; }
    public void setAvailableFrom(LocalDate v){ availableFrom = v; }
    public void setVisibility(String v)     { visibility = v; }
    public void setShareToken(String v)     { shareToken = v; }
    public void setPartnerNote(String v)    { partnerNote = v; }
    public void setHidden(Boolean v)        { hidden = v; }
    public void setFeatured(Boolean v)      { featured = v; }
    public void setCreatedAt(Instant v)     { createdAt = v; }
    public void setUpdatedAt(Instant v)     { updatedAt = v; }
}
