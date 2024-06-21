package com.oneship.chargebook.repository;

import com.oneship.chargebook.model.KiaToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KiaTokenRepository extends JpaRepository<KiaToken, String> {
}
