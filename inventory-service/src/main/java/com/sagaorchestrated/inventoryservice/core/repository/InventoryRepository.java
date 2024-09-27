package com.sagaorchestrated.inventoryservice.core.repository;

import com.sagaorchestrated.inventoryservice.core.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

  Optional<Inventory> findByProductCode(String productCode);

}
