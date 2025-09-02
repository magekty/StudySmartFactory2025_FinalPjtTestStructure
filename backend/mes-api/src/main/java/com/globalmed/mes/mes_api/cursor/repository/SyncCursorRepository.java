package com.globalmed.mes.mes_api.cursor.repository;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncCursorRepository extends JpaRepository<SyncCursorEntity, String> {}