package com.ambulance.system.repository;

import com.ambulance.system.entity.Role;
import com.ambulance.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByFullNameIgnoreCase(String fullName);
    Optional<User> findFirstByEmailStartingWithIgnoreCase(String prefix);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
