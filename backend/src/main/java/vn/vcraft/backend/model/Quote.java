package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "quotes")
public class Quote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long requestId;
  private Long workshopId;

  @Column(precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(precision = 15, scale = 2)
  private BigDecimal totalPrice;

  private Integer leadTimeDays;
  private String note;

  @Enumerated(EnumType.STRING)
  private QuoteStatus status;

  @Version private Long version;
  private Instant createdAt;
  private Instant updatedAt;

  public Quote() {}

  public Long getId() {
    return id;
  }

  public Long getRequestId() {
    return requestId;
  }

  public void setRequestId(Long value) {
    requestId = value;
  }

  public Long getWorkshopId() {
    return workshopId;
  }

  public void setWorkshopId(Long value) {
    workshopId = value;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal value) {
    unitPrice = value;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal value) {
    totalPrice = value;
  }

  public Integer getLeadTimeDays() {
    return leadTimeDays;
  }

  public void setLeadTimeDays(Integer value) {
    leadTimeDays = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }

  public QuoteStatus getStatus() {
    return status;
  }

  public void setStatus(QuoteStatus value) {
    status = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    version = value;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant value) {
    createdAt = value;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant value) {
    updatedAt = value;
  }
}
