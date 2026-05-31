package com.uts.inventario.repository;

import com.uts.inventario.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {

    List<AssetType> findByOrderByName();

    List<AssetType> findByCategoryOrderByName(String category);
}
