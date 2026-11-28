package vn.vcraft.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.Batch;

import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    /** Tìm theo public_code — dùng cho landing page và QR. */
    @Query("SELECT b FROM Batch b WHERE b.publicCode = :code")
    Optional<Batch> findByPublicCode(@Param("code") String code);

    boolean existsByPublicCode(String publicCode);

    /** Danh sách lô của xưởng (kể cả ẩn — artisan xem). */
    @Query("""
        SELECT b FROM Batch b WHERE b.workshopId = :workshopId
        ORDER BY b.createdAt DESC
        """)
    java.util.List<Batch> findByWorkshopId(@Param("workshopId") long workshopId);

    /** Danh sách lô công khai (phân trang, có filter). */
    @Query(value = """
        SELECT b FROM Batch b
        WHERE b.visibility = 'PUBLIC' AND b.hidden = FALSE AND EXISTS (SELECT w.id FROM Workshop w WHERE w.id = b.workshopId AND w.approved = TRUE AND w.lockedAt IS NULL)
          AND (:workshopId IS NULL OR b.workshopId = :workshopId)
          AND (:productId IS NULL OR b.productId = :productId)
          AND (:kind IS NULL OR b.kind = :kind)
        ORDER BY b.featured DESC, b.createdAt DESC
        """,
            countQuery = """
        SELECT COUNT(b) FROM Batch b
        WHERE b.visibility = 'PUBLIC' AND b.hidden = FALSE AND EXISTS (SELECT w.id FROM Workshop w WHERE w.id = b.workshopId AND w.approved = TRUE AND w.lockedAt IS NULL)
          AND (:workshopId IS NULL OR b.workshopId = :workshopId)
          AND (:productId IS NULL OR b.productId = :productId)
          AND (:kind IS NULL OR b.kind = :kind)
        """)
    Page<Batch> findPublicFiltered(
            @Param("workshopId") Long workshopId,
            @Param("productId") Long productId,
            @Param("kind") String kind,
            Pageable pageable);

    /** Lô nổi bật (trang chủ). */
    @Query("SELECT b FROM Batch b WHERE b.featured = TRUE AND b.hidden = FALSE AND EXISTS (SELECT w.id FROM Workshop w WHERE w.id = b.workshopId AND w.approved = TRUE AND w.lockedAt IS NULL) AND b.visibility = 'PUBLIC'")
    java.util.List<Batch> findFeatured();

    @Modifying
    @Query("UPDATE Batch b SET b.hidden = TRUE WHERE b.id = :id")
    void hide(@Param("id") long id);

    @Modifying
    @Query("UPDATE Batch b SET b.featured = FALSE WHERE b.featured = TRUE")
    void clearAllFeatured();

    @Modifying
    @Query("UPDATE Batch b SET b.featured = :featured WHERE b.id = :id")
    void setFeatured(@Param("id") long id, @Param("featured") boolean featured);
}
