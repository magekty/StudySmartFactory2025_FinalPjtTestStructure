package com.globalmed.mes.mes_api.cursor.repository;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncCursorRepository extends JpaRepository<SyncCursor, String> {}