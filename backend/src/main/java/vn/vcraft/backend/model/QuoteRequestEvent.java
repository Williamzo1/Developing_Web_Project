package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "quote_request_events")
public class QuoteRequestEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long requestId;
  private Long actorAccountId;

  @Enumerated(EnumType.STRING)
  private RequestStatus fromStatus;

  @Enumerated(EnumType.STRING)
  private RequestStatus toStatus;

  private String note;
  private Instant createdAt;

  public QuoteRequestEvent() {}

  public Long getId() {
    return id;
  }

  public Long getRequestId() {
    return requestId;
  }

  public void setRequestId(Long value) {
    requestId = value;
  }

  public Long getActorAccountId() {
    return actorAccountId;
  }

  public void setActorAccountId(Long value) {
    actorAccountId = value;
  }

  public RequestStatus getFromStatus() {
    return fromStatus;
  }

  public void setFromStatus(RequestStatus value) {
    fromStatus = value;
  }

  public RequestStatus getToStatus() {
    return toStatus;
  }

  public void setToStatus(RequestStatus value) {
    toStatus = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant value) {
    createdAt = value;
  }
}
