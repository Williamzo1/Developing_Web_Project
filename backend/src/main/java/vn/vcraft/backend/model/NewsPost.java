package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "news_posts")
public class NewsPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long authorAccountId;
    private String slug;
    private String title;
    private String body;
    private String kind;
    private Instant eventStart;
    private Instant eventEnd;
    private String eventPlace;
    private Boolean hidden;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public NewsPost() {}

    public Long getId()              { return id; }
    public Long getAuthorAccountId() { return authorAccountId; }
    public String getSlug()          { return slug; }
    public String getTitle()         { return title; }
    public String getBody()          { return body; }
    public String getKind()          { return kind; }
    public Instant getEventStart()   { return eventStart; }
    public Instant getEventEnd()     { return eventEnd; }
    public String getEventPlace()    { return eventPlace; }
    public Boolean getHidden()       { return hidden; }
    public Instant getPublishedAt()  { return publishedAt; }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }

    public void setAuthorAccountId(Long v) { authorAccountId = v; }
    public void setSlug(String v)          { slug = v; }
    public void setTitle(String v)         { title = v; }
    public void setBody(String v)          { body = v; }
    public void setKind(String v)          { kind = v; }
    public void setEventStart(Instant v)   { eventStart = v; }
    public void setEventEnd(Instant v)     { eventEnd = v; }
    public void setEventPlace(String v)    { eventPlace = v; }
    public void setHidden(Boolean v)       { hidden = v; }
    public void setPublishedAt(Instant v)  { publishedAt = v; }
    public void setCreatedAt(Instant v)    { createdAt = v; }
    public void setUpdatedAt(Instant v)    { updatedAt = v; }
}
