package com.thanh.foodOrder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodOrder.domain.Role;
import com.thanh.foodOrder.domain.respone.ResultPaginationDTO;
import com.thanh.foodOrder.dtos.response.auth.ResponseRoleDTO;
import com.thanh.foodOrder.service.RoleService;
import com.thanh.foodOrder.util.anotation.ApiMessage;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")

public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @ApiMessage("Create a new role")
    public ResponseEntity<ResponseRoleDTO> handleCreateRole(
            @RequestBody Role role) {

        return ResponseEntity.ok().body(this.roleService.createRole(role));
    }

    @PutMapping("/roles")
    @ApiMessage("Update a role")
    public ResponseEntity<ResponseRoleDTO> handleUpdateRole(@RequestBody Role role) {
        return ResponseEntity.ok().body(this.roleService.updateRole(role));
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete a role")
    public ResponseEntity<Void> handleDeleteRole(@PathVariable("id") Long id) {
        this.roleService.deleteRole(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<ResponseRoleDTO> handleGetRoleById(@PathVariable("id") Long id) {
        Role role = this.roleService.getRoleById(id);
        return ResponseEntity.ok().body(this.roleService.converRoleToResRoleDTO(role));

    }

    @GetMapping("/roles")
    public ResponseEntity<ResultPaginationDTO> handleGetAllRole(Pageable pageable) {

        return ResponseEntity.ok().body(this.roleService.getAllRole(pageable));
    }

}
