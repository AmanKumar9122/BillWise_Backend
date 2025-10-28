package com.aksps.BillWise.repository;


import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// why extending JpaRepository?
// JpaRepository provides CRUD operations and pagination for the Role entity
public interface RoleRepository extends JpaRepository<Role, Long> {

    // why Optional?
    // Optional is used to handle the case where a role with the specified name may not exist
    // Method to find a role by its name
    Optional<Role> findByName(RoleName name);
}
