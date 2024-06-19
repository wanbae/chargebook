package com.oneship.chargebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oneship.chargebook.model.ChargeData;

@Repository
public interface ChargeDataRepository extends JpaRepository<ChargeData, Long> {

    @Query("SELECT c FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 ORDER BY c.date DESC")
    List<ChargeData> findByMonth(String month);

    @Query("SELECT COALESCE(SUM(c.distance), 0) FROM ChargeData c")
    int getAccumulatedDistance();

    @Query("SELECT c.company, SUM(c.amountOfCharge) FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 GROUP BY c.company")
    List<Object[]> findTotalChargeByCompany(String month);

    @Query("SELECT c.card, SUM(c.finalPrice) FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 GROUP BY c.card")
    List<Object[]> findTotalPriceByCard(String month);
}
