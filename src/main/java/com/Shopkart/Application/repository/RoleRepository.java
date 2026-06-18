package com.Shopkart.Application.repository;

import com.Shopkart.Application.model.AppRole;
import com.Shopkart.Application.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
