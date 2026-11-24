package vn.vcraft.backend.repository;

import vn.vcraft.backend.model.Product;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p join fetch p.workshop join fetch p.category where p.hidden = false AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL order by p.id")
    List<Product> findCatalog();

    @Query(value = "select p from Product p join fetch p.workshop join fetch p.category where p.hidden = false AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL",
           countQuery = "select count(p) from Product p where p.hidden = false AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL")
    org.springframework.data.domain.Page<Product> findCatalogPage(org.springframework.data.domain.Pageable pageable);

    @Query("select p from Product p join fetch p.workshop join fetch p.category where p.id = :id AND p.hidden = false AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL")
    Optional<Product> findCatalogProduct(long id);
    // Thêm vào ProductRepository (JPA interface)

    /** Tìm theo slug — public (không ẩn). */
    @Query("""
    SELECT p FROM Product p
    JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.slug = :slug AND p.hidden = FALSE AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL
    """)
    Optional<Product> findBySlugPublic(@Param("slug") String slug);

    /** Tìm theo slug — artisan (kể cả ẩn, kiểm tra ownership ở Service). */
    @Query("""
    SELECT p FROM Product p
    JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.slug = :slug
    """)
    Optional<Product> findBySlug(@Param("slug") String slug);

    /** Phân trang có filter — public. */
    @Query(value = """
    SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.hidden = FALSE AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL
      AND (:workshopId IS NULL OR p.workshop.id = :workshopId)
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """,
            countQuery = """
    SELECT COUNT(p) FROM Product p
    WHERE p.hidden = FALSE AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL
      AND (:workshopId IS NULL OR p.workshop.id = :workshopId)
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    Page<Product> findFiltered(
            @Param("workshopId") Long workshopId,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /** Tất cả sản phẩm của xưởng (artisan). */
    @Query("""
    SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.workshop.id = :workshopId ORDER BY p.createdAt DESC
    """)
    List<Product> findByWorkshopAll(@Param("workshopId") long workshopId);

    /** Kiểm tra slug đã tồn tại chưa (dùng trong SlugUtils). */
    boolean existsBySlug(String slug);

    /** Nổi bật. */
    @Query("SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category WHERE p.featured = TRUE AND p.hidden = FALSE AND p.workshop.approved = TRUE AND p.workshop.lockedAt IS NULL")
    List<Product> findFeatured();

    @Modifying
    @Query("UPDATE Product p SET p.hidden = TRUE WHERE p.id = :id")
    void hide(@Param("id") long id);

    @Modifying
    @Query("UPDATE Product p SET p.featured = FALSE WHERE p.featured = TRUE")
    void clearAllFeatured();

    @Modifying
    @Query("UPDATE Product p SET p.featured = :v WHERE p.id = :id")
    void setFeatured(@Param("id") long id, @Param("v") boolean v);

    @Query(value = """
        SELECT p.* FROM products p JOIN workshops w ON w.id=p.workshop_id
        JOIN product_tags pt ON pt.product_id=p.id JOIN tags t ON t.id=pt.tag_id
        WHERE p.hidden=FALSE AND w.approved=TRUE AND w.locked_at IS NULL
          AND (:workshopId IS NULL OR p.workshop_id=:workshopId)
          AND (:categoryId IS NULL OR p.category_id=:categoryId) AND t.slug=:tagSlug
        """, countQuery = """
        SELECT count(*) FROM products p JOIN workshops w ON w.id=p.workshop_id
        JOIN product_tags pt ON pt.product_id=p.id JOIN tags t ON t.id=pt.tag_id
        WHERE p.hidden=FALSE AND w.approved=TRUE AND w.locked_at IS NULL
          AND (:workshopId IS NULL OR p.workshop_id=:workshopId)
          AND (:categoryId IS NULL OR p.category_id=:categoryId) AND t.slug=:tagSlug
        """, nativeQuery=true)
    Page<Product> findByTag(@Param("workshopId") Long workshopId,
            @Param("categoryId") Long categoryId, @Param("tagSlug") String tagSlug, Pageable pageable);
}
