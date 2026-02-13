package com.mestro.repository;

import com.mestro.model.Warehouse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByName(String name);

    List<Warehouse> findByIsActive(Boolean isActive);

    List<Warehouse> findByCity(String city);

    boolean existsByName(String name);
}
