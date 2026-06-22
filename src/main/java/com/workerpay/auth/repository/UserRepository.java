package com.workerpay.auth.repository;

import com.workerpay.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("select count(u) > 0 from User u join u.roles r where r.name = 'ADMIN'")
    boolean existsAdminUser();
}
