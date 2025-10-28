package com.aksps.BillWise.repository;


import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
