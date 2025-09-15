package com.factory_dynamics.erp.erp_server.cost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CostSnapshotDetailRepository extends JpaRepository<CostSnapshotDetail, Long> {

    @Query("""
        select d from CostSnapshotDetail d
        join fetch d.component c
        where d.snapshot.id = :snapshotId
        """)
    List<CostSnapshotDetail> findAllWithComponentBySnapshotId(String snapshotId);
}