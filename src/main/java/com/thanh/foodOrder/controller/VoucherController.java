package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.Voucher;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.service.VoucherService;
import com.thanh.foodorder.util.annotation.ApiMessage;

import org.springframework.http.HttpStatus;
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
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/vouchers")
    @ApiMessage("Create a voucher")
    public ResponseEntity<Voucher> handleCreateVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.voucherService.createVoucher(voucher));
    }

    @PutMapping("/vouchers")
    public ResponseEntity<Voucher> handleUpdateVoucer(@RequestBody Voucher voucher) {

        return ResponseEntity.status(HttpStatus.OK).body(this.voucherService.updatVoucher(voucher));
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<Void> handleDeleteVoucher(@PathVariable("id") Long id) {
        this.voucherService.delteVoucherById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/vouchers/{id}")
    public ResponseEntity<Voucher> handleGetVoucherById(@PathVariable("id") Long id) {
        Voucher voucher = this.voucherService.getVoucherById(id);
        return ResponseEntity.status(HttpStatus.OK).body(voucher);
    }

    @GetMapping("/vouchers")
    public ResponseEntity<ResultPaginationDTO> handleGetAllVouchers(
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "5", required = false) Integer size,
            @RequestParam(name = "code", required = false) String code) {

        ResultPaginationDTO rs = this.voucherService.getAllVouchers(page, size, code);

        return ResponseEntity.status(HttpStatus.OK).body(rs);
    }

}
