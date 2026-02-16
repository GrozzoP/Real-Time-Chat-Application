package com.grosso.chat.service;

import com.grosso.chat.model.ERole;
import com.grosso.chat.model.Role;
import com.grosso.chat.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public void save(Role role) {
        roleRepository.save(role);
    }

    public Optional<Role> getRoleByName(ERole name) {
        return roleRepository.findByName(name);
    }
}
