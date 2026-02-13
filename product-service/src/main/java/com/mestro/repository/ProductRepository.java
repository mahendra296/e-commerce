package com.mestro.repository;

import com.mestro.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByIsActive(Boolean isActive);

    List<Product> findByBrand(String brand);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :productId")
    Optional<Product> findByIdWithCategory(@Param("productId") Long productId);
}
