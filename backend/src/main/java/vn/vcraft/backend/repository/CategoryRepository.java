package vn.vcraft.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.vcraft.backend.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
}
