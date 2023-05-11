package com.soa_rest.models.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soa_rest.models.entities.User;

public interface UserRepo extends JpaRepository<User, Integer> {
    // find by username
    Optional<User> findByUsername(String username);
}
