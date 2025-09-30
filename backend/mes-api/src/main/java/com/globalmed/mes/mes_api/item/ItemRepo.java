package com.globalmed.mes.mes_api.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepo extends JpaRepository<ItemEntity, String> {

    @Query("SELECT i.itemName FROM ItemEntity i WHERE i.itemId = :itemId AND i.isDeleted = false")
    String findItemNameByItemId(@Param("itemId") String itemId);
}
