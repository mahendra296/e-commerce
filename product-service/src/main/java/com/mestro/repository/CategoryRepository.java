package com.mestro.repository;

import com.mestro.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByIsActive(Boolean isActive);

    List<Category> findByParentCategoryId(Long parentCategoryId);

    boolean existsByName(String name);
}
