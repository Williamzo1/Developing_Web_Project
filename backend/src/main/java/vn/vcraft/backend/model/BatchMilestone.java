package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "batch_milestones")
public class BatchMilestone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long batchId;
    private Long processStepId;
    private String title;
    private String note;
    private LocalDate happenedOn;
    private Instant createdAt;

    public BatchMilestone() {}

    public Long getId()              { return id; }
    public Long getBatchId()         { return batchId; }
    public Long getProcessStepId()   { return processStepId; }
    public String getTitle()         { return title; }
    public String getNote()          { return note; }
    public LocalDate getHappenedOn() { return happenedOn; }
    public Instant getCreatedAt()    { return createdAt; }

    public void setBatchId(Long v)        { batchId = v; }
    public void setProcessStepId(Long v)  { processStepId = v; }
    public void setTitle(String v)        { title = v; }
    public void setNote(String v)         { note = v; }
    public void setHappenedOn(LocalDate v){ happenedOn = v; }
    public void setCreatedAt(Instant v)   { createdAt = v; }
}
