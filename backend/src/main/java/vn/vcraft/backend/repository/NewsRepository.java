package vn.vcraft.backend.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.NewsPost;
import java.util.Optional;
import java.util.List;

public interface NewsRepository extends JpaRepository<NewsPost, Long> {
    boolean existsBySlug(String slug);

    @Query(value = """
        SELECT n FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
          AND (:kind IS NULL OR n.kind = :kind)
        ORDER BY n.publishedAt DESC
        """,
            countQuery = """
        SELECT COUNT(n) FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
          AND (:kind IS NULL OR n.kind = :kind)
        """)
    Page<NewsPost> findPublished(@Param("kind") String kind, Pageable pageable);

    @Query("""
        SELECT n FROM NewsPost n WHERE n.slug = :slug
        AND n.publishedAt IS NOT NULL AND n.hidden = FALSE
        """)
    Optional<NewsPost> findBySlugPublic(@Param("slug") String slug);

    @Query("SELECT n FROM NewsPost n ORDER BY n.createdAt DESC")
    Page<NewsPost> findAllForAdmin(Pageable pageable);

    @Query("""
        SELECT n FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
        ORDER BY n.publishedAt DESC
        """)
    List<NewsPost> findLatest(Pageable pageable);
}
