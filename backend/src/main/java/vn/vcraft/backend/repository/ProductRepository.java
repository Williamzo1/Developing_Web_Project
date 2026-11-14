package vn.vcraft.backend.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import vn.vcraft.backend.dto.CatalogOption;
import vn.vcraft.backend.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    String FETCH = "select p from Product p join fetch p.workshop join fetch p.category";
    String VISIBLE = " where p.workshop.approved = true and p.hidden = false and p.adminHidden = false";
    String FILTERS = """
            and (:workshop is null or p.workshop.slug = :workshop)
            and (:category is null or p.category.slug = :category)
            and locate(lower(:q), lower(p.name)) > 0
            """;

    @Query(FETCH + VISIBLE + " order by p.id")
    List<Product> findCatalog();

    @Query(value = FETCH + VISIBLE + " " + FILTERS, countQuery = "select count(p) from Product p" + VISIBLE + " " + FILTERS)
    Page<Product> findCatalogPage(String workshop, String category, String q, Pageable pageable);

    @Query(FETCH + VISIBLE + " and p.id = :id")
    Optional<Product> findCatalogProduct(long id);

    @Query(FETCH + VISIBLE + " and p.slug = :slug")
    Optional<Product> findCatalogProduct(String slug);

    @Query(value = FETCH + " where p.workshop.id = :workshopId",
           countQuery = "select count(p) from Product p where p.workshop.id = :workshopId")
    Page<Product> findOwned(long workshopId, Pageable pageable);

    @Query(FETCH + " where p.workshop.id = :workshopId and p.id = :id")
    Optional<Product> findOwned(long workshopId, long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.workshop.id = :workshopId and p.id = :id")
    Optional<Product> lockOwned(long workshopId, long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> lockById(long id);

    @Query("select new vn.vcraft.backend.dto.CatalogOption(c.slug, c.name) from Category c order by c.name, c.id")
    List<CatalogOption> findCategoryOptions();
}
