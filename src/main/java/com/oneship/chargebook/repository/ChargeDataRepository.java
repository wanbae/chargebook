package com.oneship.chargebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oneship.chargebook.model.ChargeData;

@Repository
public interface ChargeDataRepository extends JpaRepository<ChargeData, Long> {

    @Query("SELECT c FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1")
    List<ChargeData> findByMonth(String month);

    @Query("SELECT COALESCE(SUM(c.distance), 0) FROM ChargeData c")
    int getAccumulatedDistance();
}
