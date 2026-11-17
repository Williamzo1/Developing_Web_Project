package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "quote_requests")
public class QuoteRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long buyerAccountId;
  private Long categoryId;
  private String title;
  private Integer quantity;
  private String unit;
  private String spec;
  private String note;
  private LocalDate desiredBy;
  private Instant biddingDeadline;

  @Enumerated(EnumType.STRING)
  private RequestMode mode;

  private Long targetWorkshopId;
  private String buyerContactName;
  private String buyerContactEmail;
  private String buyerContactPhone;

  @Enumerated(EnumType.STRING)
  private RequestStatus status;

  private Long acceptedQuoteId;
  @Version private Long version;
  private Instant createdAt;
  private Instant updatedAt;

  public QuoteRequest() {}

  public Long getId() {
    return id;
  }

  public Long getBuyerAccountId() {
    return buyerAccountId;
  }

  public void setBuyerAccountId(Long value) {
    buyerAccountId = value;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long value) {
    categoryId = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    title = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    quantity = value;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String value) {
    unit = value;
  }

  public String getSpec() {
    return spec;
  }

  public void setSpec(String value) {
    spec = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }

  public LocalDate getDesiredBy() {
    return desiredBy;
  }

  public void setDesiredBy(LocalDate value) {
    desiredBy = value;
  }

  public Instant getBiddingDeadline() {
    return biddingDeadline;
  }

  public void setBiddingDeadline(Instant value) {
    biddingDeadline = value;
  }

  public RequestMode getMode() {
    return mode;
  }

  public void setMode(RequestMode value) {
    mode = value;
  }

  public Long getTargetWorkshopId() {
    return targetWorkshopId;
  }

  public void setTargetWorkshopId(Long value) {
    targetWorkshopId = value;
  }

  public String getBuyerContactName() {
    return buyerContactName;
  }

  public void setBuyerContactName(String value) {
    buyerContactName = value;
  }

  public String getBuyerContactEmail() {
    return buyerContactEmail;
  }

  public void setBuyerContactEmail(String value) {
    buyerContactEmail = value;
  }

  public String getBuyerContactPhone() {
    return buyerContactPhone;
  }

  public void setBuyerContactPhone(String value) {
    buyerContactPhone = value;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public void setStatus(RequestStatus value) {
    status = value;
  }

  public Long getAcceptedQuoteId() {
    return acceptedQuoteId;
  }

  public void setAcceptedQuoteId(Long value) {
    acceptedQuoteId = value;
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
