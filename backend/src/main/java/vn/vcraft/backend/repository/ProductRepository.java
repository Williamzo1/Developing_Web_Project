package vn.vcraft.backend.repository;

import vn.vcraft.backend.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p join fetch p.workshop join fetch p.category order by p.id")
    List<Product> findCatalog();

    @Query("select p from Product p join fetch p.workshop join fetch p.category where p.id = :id")
    Optional<Product> findCatalogProduct(long id);
}
