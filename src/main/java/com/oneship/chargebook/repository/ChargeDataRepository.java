package com.oneship.chargebook.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;

@Repository
public interface ChargeDataRepository extends JpaRepository<ChargeData, Long> {

    @Query("SELECT c FROM ChargeData c WHERE c.date BETWEEN ?1 AND ?2 AND c.user = ?3 ORDER BY c.date DESC")
    List<ChargeData> findByDateRangeAndUser(Date startDate, Date endDate, User user);

    @Query(value = "SELECT SUM(c.distance) FROM ChargeData c WHERE c.user_id = ?1", nativeQuery = true)
    long getAccumulatedDistance(User user);

    @Query("SELECT c.company, SUM(c.amountOfCharge) FROM ChargeData c WHERE c.date BETWEEN ?1 AND ?2 AND c.user = ?3 GROUP BY c.company")
    List<Object[]> findTotalChargeByCompany(Date startDate, Date endDate, User user);

    @Query("SELECT c.card, SUM(c.finalPrice) FROM ChargeData c WHERE c.date BETWEEN ?1 AND ?2 AND c.user = ?3 GROUP BY c.card")
    List<Object[]> findTotalPriceByCard(Date startDate, Date endDate, User user);

    List<ChargeData> findByUser(User user);

    Optional<ChargeData> findByIdAndUser(Long id, User user);
}
