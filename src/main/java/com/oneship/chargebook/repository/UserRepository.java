
package com.oneship.chargebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneship.chargebook.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}