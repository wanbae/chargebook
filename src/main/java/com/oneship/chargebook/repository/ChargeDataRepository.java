package com.oneship.chargebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;

@Repository
public interface ChargeDataRepository extends JpaRepository<ChargeData, Long> {

    @Query("SELECT c FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 AND c.user = ?2 ORDER BY c.date DESC")
    List<ChargeData> findByMonthAndUser(String month, User user);

    @Query("SELECT COALESCE(SUM(c.distance), 0) FROM ChargeData c WHERE c.user = ?1")
    int getAccumulatedDistance(User user);

    @Query("SELECT c.company, SUM(c.amountOfCharge) FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 AND c.user = ?2 GROUP BY c.company")
    List<Object[]> findTotalChargeByCompany(String month, User user);

    @Query("SELECT c.card, SUM(c.finalPrice) FROM ChargeData c WHERE FUNCTION('FORMATDATETIME', c.date, 'yyyy-MM') = ?1 AND c.user = ?2 GROUP BY c.card")
    List<Object[]> findTotalPriceByCard(String month, User user);

    List<ChargeData> findByUser(User user);

    Optional<ChargeData> findByIdAndUser(Long id, User user);
}
