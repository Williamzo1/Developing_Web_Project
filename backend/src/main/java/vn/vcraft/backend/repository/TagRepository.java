package vn.vcraft.backend.repository;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.*;
@Repository
public class TagRepository {
    private final JdbcTemplate db;
    public TagRepository(JdbcTemplate db) { this.db=db; }
    public List<TagResponse> list() { return db.query("SELECT id,slug,name FROM tags ORDER BY id",(rs,n)->new TagResponse(rs.getLong(1),rs.getString(2),rs.getString(3))); }
    public TagResponse create(TagInput i) {
        return db.queryForObject("INSERT INTO tags(slug,name) VALUES (?,?) RETURNING id,slug,name",(rs,n)->new TagResponse(rs.getLong(1),rs.getString(2),rs.getString(3)),i.slug(),i.name());
    }
    public java.util.Optional<TagResponse> update(long id,TagInput i) {
        return db.query("UPDATE tags SET slug=?,name=? WHERE id=? RETURNING id,slug,name",(rs,n)->new TagResponse(rs.getLong(1),rs.getString(2),rs.getString(3)),i.slug(),i.name(),id).stream().findFirst();
    }
    public boolean delete(long id) { return db.update("DELETE FROM tags WHERE id=?",id)>0; }
    public long countQuoteRequests() { return db.queryForObject("SELECT count(*) FROM quote_requests",Long.class); }
}
