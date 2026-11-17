package vn.vcraft.backend.repository;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.*;

@Repository
public class QuoteMatchingRepository {
  private final JdbcTemplate db;

  public QuoteMatchingRepository(JdbcTemplate db) {
    this.db = db;
  }

  // Q-38: change ONLY this function when tag matching is decided.
  private String eligibilityPredicate() {
    return """
           ((r.mode='TARGETED' AND r.target_workshop_id=w.id) OR
            (r.mode='OPEN' AND EXISTS (
              SELECT 1 FROM products p JOIN product_tags pt ON pt.product_id=p.id
              JOIN quote_request_tags rt ON rt.tag_id=pt.tag_id
              WHERE p.workshop_id=w.id AND rt.request_id=r.id
            )))
           """;
  }

  public boolean isEligible(long requestId, long workshopId) {
    return Boolean.TRUE.equals(
        db.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM quote_requests r JOIN workshops w ON w.id=? AND w.approved"
                + " WHERE r.id=? AND "
                + eligibilityPredicate()
                + ")",
            Boolean.class,
            workshopId,
            requestId));
  }

  public org.springframework.data.domain.Page<Long> visiblePage(
      long workshopId, int page, int size) {
    String from =
        " FROM quote_requests r JOIN workshops w ON w.id=? AND w.approved WHERE "
            + "EXISTS(SELECT 1 FROM quotes q WHERE q.request_id=r.id AND q.workshop_id=w.id) OR "
            + "(r.status<>'DRAFT' AND (r.mode='TARGETED' OR r.status IN ('SENT','BIDDING')) AND "
            + eligibilityPredicate()
            + ")";
    // Parenthesize the visibility predicate to keep approval applied to every branch.
    Long total = db.queryForObject("SELECT count(*)" + from, Long.class, workshopId);
    List<Long> ids =
        db.queryForList(
            "SELECT r.id" + from + " ORDER BY r.id DESC LIMIT ? OFFSET ?",
            Long.class,
            workshopId,
            size,
            (long) page * size);
    return new org.springframework.data.domain.PageImpl<>(
        ids, org.springframework.data.domain.PageRequest.of(page, size), total);
  }

  public boolean approved(long id) {
    return Boolean.TRUE.equals(
        db.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM workshops WHERE id=? AND approved)", Boolean.class, id));
  }

  public boolean categoryExists(long id) {
    return Boolean.TRUE.equals(
        db.queryForObject("SELECT EXISTS(SELECT 1 FROM categories WHERE id=?)", Boolean.class, id));
  }

  public boolean tagExists(long id) {
    return Boolean.TRUE.equals(
        db.queryForObject("SELECT EXISTS(SELECT 1 FROM tags WHERE id=?)", Boolean.class, id));
  }

  public List<Long> tags(long id) {
    return db.queryForList(
        "SELECT tag_id FROM quote_request_tags WHERE request_id=? ORDER BY tag_id", Long.class, id);
  }

  public void replaceTags(long id, List<Long> ids) {
    db.update("DELETE FROM quote_request_tags WHERE request_id=?", id);
    for (Long tag : ids)
      db.update("INSERT INTO quote_request_tags(request_id,tag_id) VALUES (?,?)", id, tag);
  }

  public ContactResponse contact(long workshopId) {
    return db.queryForObject(
        "SELECT name,contact_email,phone FROM workshops WHERE id=?",
        (rs, n) -> new ContactResponse(rs.getString(1), rs.getString(2), rs.getString(3)),
        workshopId);
  }

  public List<QuoteCategoryResponse> categoryOptions() {
    return db.query(
        "SELECT id,slug,name FROM categories ORDER BY id",
        (rs, n) -> new QuoteCategoryResponse(rs.getLong(1), rs.getString(2), rs.getString(3)));
  }

  public List<QuoteTagResponse> tagOptions() {
    return db.query(
        "SELECT id,slug,name FROM tags ORDER BY id",
        (rs, n) -> new QuoteTagResponse(rs.getLong(1), rs.getString(2), rs.getString(3)));
  }
}
