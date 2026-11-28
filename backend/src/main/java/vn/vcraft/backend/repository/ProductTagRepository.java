package vn.vcraft.backend.repository;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.exception.ApiException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
@Repository
public class ProductTagRepository {
    private final JdbcTemplate db;
    public ProductTagRepository(JdbcTemplate db) { this.db = db; }
    public void replaceTags(long productId, List<Long> ids) {
        for (Long id : ids) {
            if (id == null || db.queryForObject("SELECT count(*) FROM tags WHERE id=?", Long.class, id) == 0)
                throw new ApiException(BAD_REQUEST, "INVALID_INPUT", "Tag không hợp lệ.");
        }
        db.update("DELETE FROM product_tags WHERE product_id=?", productId);
        ids.stream().distinct().forEach(id -> db.update("INSERT INTO product_tags(product_id,tag_id) VALUES (?,?)", productId,id));
    }
    public List<String> names(long productId) {
        return db.queryForList("SELECT t.name FROM tags t JOIN product_tags pt ON t.id=pt.tag_id WHERE pt.product_id=? ORDER BY t.id", String.class, productId);
    }
}
