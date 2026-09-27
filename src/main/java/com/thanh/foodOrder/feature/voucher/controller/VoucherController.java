package com.thanh.foodorder.feature.voucher.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.annotation.ApiMessage;
import com.thanh.foodorder.feature.voucher.domain.Voucher;
import com.thanh.foodorder.feature.voucher.dto.ApplyVoucherRequest;
import com.thanh.foodorder.feature.voucher.dto.ApplyVoucherResponse;
import com.thanh.foodorder.feature.voucher.dto.UpdateVoucherStatusDTO;
import com.thanh.foodorder.feature.voucher.enums.VoucherStatus;
import com.thanh.foodorder.feature.voucher.service.VoucherService;

@RestController
@RequestMapping("/api/v1")
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/vouchers")
    @ApiMessage("Create a voucher")
    public ResponseEntity<Voucher> handleCreateVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.voucherService.createVoucher(voucher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/vouchers")
    @ApiMessage("Update a voucher ")
    public ResponseEntity<Voucher> handleUpdateVoucer(@RequestBody Voucher voucher) {

        return ResponseEntity.status(HttpStatus.OK).body(this.voucherService.updatVoucher(voucher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/vouchers/{id}/status")
    @ApiMessage("Update voucher status")
    public ResponseEntity<Voucher> handleUpdateVoucherStatus(
            @PathVariable("id") Long id,
            @RequestParam(name = "status", required = false) VoucherStatus statusParam,
            @RequestBody(required = false) UpdateVoucherStatusDTO dto) {
        VoucherStatus targetStatus = (statusParam != null) ? statusParam : ((dto != null) ? dto.getStatus() : null);
        Voucher updatedVoucher = this.voucherService.updateVoucherStatus(id, targetStatus);
        return ResponseEntity.status(HttpStatus.OK).body(updatedVoucher);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/vouchers/{id}")
    @ApiMessage("Delete a voucher")
    public ResponseEntity<Void> handleDeleteVoucher(@PathVariable("id") Long id) {
        this.voucherService.delteVoucherById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vouchers/{id}")
    public ResponseEntity<Voucher> handleGetVoucherById(@PathVariable("id") Long id) {
        Voucher voucher = this.voucherService.getVoucherById(id);
        return ResponseEntity.status(HttpStatus.OK).body(voucher);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vouchers")
    public ResponseEntity<ResultPaginationDTO> handleGetAllVouchers(
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "5", required = false) Integer size,
            @RequestParam(name = "code", required = false) String code) {

        ResultPaginationDTO rs = this.voucherService.getAllVouchers(page, size, code);

        return ResponseEntity.status(HttpStatus.OK).body(rs);
    }

    @PostMapping("/apply-vouchers")
    public ResponseEntity<ApplyVoucherResponse> applyVoucher(
            @RequestBody ApplyVoucherRequest request) {
        return ResponseEntity.ok(
                voucherService.applyVoucher(request));
    }
}
