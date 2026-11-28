package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "workshops")
public class Workshop {
    @Id
    private Long id;
    private String slug;
    private String name;
    private String description;
    private String familyHistory;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactEmail;
    private String phone;
    private String facebookUrl;
    private Boolean approved;
    private Instant approvedAt;
    private Instant lockedAt;
    private String lockedReason;
    private Boolean featured;
    private Long coverMediaId;
    private Boolean acceptsVisitors;
    private Instant createdAt;
    private Instant updatedAt;

    protected Workshop() {}

    // Getters
    public Long getId()            { return id; }
    public String slug() { return slug; }
    public String getSlug()        { return slug; }
    public String name()           { return name; }   // giữ method name() cho code cũ dùng
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getFamilyHistory(){ return familyHistory; }
    public String getAddress()     { return address; }
    public BigDecimal getLatitude(){ return latitude; }
    public BigDecimal getLongitude(){ return longitude; }
    public String getContactEmail(){ return contactEmail; }
    public String getPhone()       { return phone; }
    public String getFacebookUrl() { return facebookUrl; }
    public Boolean getApproved()   { return approved; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getLockedAt()   { return lockedAt; }
    public String getLockedReason(){ return lockedReason; }
    public Boolean getFeatured()   { return featured; }
    public Long getCoverMediaId()  { return coverMediaId; }
    public Boolean getAcceptsVisitors(){ return acceptsVisitors; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }

    // Setters (dùng trong Service)
    public void setName(String v)           { name = v; }
    public void setDescription(String v)    { description = v; }
    public void setFamilyHistory(String v)  { familyHistory = v; }
    public void setAddress(String v)        { address = v; }
    public void setLatitude(BigDecimal v)   { latitude = v; }
    public void setLongitude(BigDecimal v)  { longitude = v; }
    public void setContactEmail(String v)   { contactEmail = v; }
    public void setPhone(String v)          { phone = v; }
    public void setFacebookUrl(String v)    { facebookUrl = v; }
    public void setApproved(Boolean v)      { approved = v; }
    public void setApprovedAt(Instant v)    { approvedAt = v; }
    public void setLockedAt(Instant v)      { lockedAt = v; }
    public void setLockedReason(String v)   { lockedReason = v; }
    public void setFeatured(Boolean v)      { featured = v; }
    public void setCoverMediaId(Long v)     { coverMediaId = v; }
    public void setAcceptsVisitors(Boolean v){ acceptsVisitors = v; }
    public void setUpdatedAt(Instant v)     { updatedAt = v; }
}
