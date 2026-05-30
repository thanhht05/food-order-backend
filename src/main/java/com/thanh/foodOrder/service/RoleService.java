package com.thanh.foodOrder.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.thanh.foodOrder.domain.Role;
import com.thanh.foodOrder.domain.respone.ResultPaginationDTO;
import com.thanh.foodOrder.dtos.response.auth.ResponseRoleDTO;
import com.thanh.foodOrder.repository.RoleRepository;
import com.thanh.foodOrder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleById(long id) {
        return this.roleRepository.findById(id).orElseThrow(() -> {
            log.warn("Role with id: {} not found", id);
            return new CommonException("Role with id " + id + " not found");

        });
    }

    public ResponseRoleDTO createRole(Role role) {
        log.info("Creating role with name: {}", role.getName());
        if (this.roleRepository.existsByName(role.getName())) {
            log.warn("Role name: {} already exists", role.getName());
            throw new CommonException("Role " + role.getName() + " already exists");
        }
        Role savedRole = this.roleRepository.save(role);
        log.info("Role created successfully with id {}", role.getId());
        return this.converRoleToResRoleDTO(savedRole);
    }

    public ResponseRoleDTO converRoleToResRoleDTO(Role role) {
        ResponseRoleDTO res = new ResponseRoleDTO();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setCreatedAt(role.getCreatedAt());
        res.setCreatedBy(role.getCreatedBy());
        res.setUpdatedAt(role.getUpdatedAt());
        res.setUpdatedBy(role.getUpdatedBy());
        return res;
    }

    public ResponseRoleDTO updateRole(Role role) {
        log.info("Updating role with id: {}", role.getId());
        Role existingRole = this.getRoleById(role.getId());
        existingRole.setName(role.getName());

        Role updatedRole = this.roleRepository.save(existingRole);
        log.info("Role updated ", updatedRole);
        return this.converRoleToResRoleDTO(updatedRole);
    }

    public void deleteRole(long id) {
        log.info("Deleting role with id: {}", id);
        Role role = this.getRoleById(id);
        this.roleRepository.delete(role);
        log.info("Role deleted successfully with id {}", id);
    }

    public ResultPaginationDTO getAllRole(Pageable pageable) {
        log.info("Fetching all role");
        Specification<Role> spec = Specification.allOf();
        Page<Role> roles = this.roleRepository.findAll(spec, pageable);

        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(roles.getTotalPages());
        meta.setTotalElements(roles.getTotalElements());
        res.setResults(roles.getContent());
        res.setMeta(meta);

        log.info("All role fetched successfully");

        return res;
    }
}
