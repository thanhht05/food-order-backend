package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.BookingTable;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.service.BookingTableService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class BookingTableController {
    private final BookingTableService bookingTableService;

    public BookingTableController(BookingTableService bookingTableService) {
        this.bookingTableService = bookingTableService;
    }

    @PostMapping("/bookingTables")
    public ResponseEntity<BookingTable> handleCreateTable(@RequestBody BookingTable bookingTable) {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookingTableService.createTable(bookingTable));
    }

    @PutMapping("/bookingTables")
    public ResponseEntity<BookingTable> handleUpdateTable(@RequestBody BookingTable bookingTable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.bookingTableService.updateTable(bookingTable));
    }

    @DeleteMapping("/bookingTables/{id}")
    public ResponseEntity<Void> handleDeleteTable(@PathVariable("id") Long id) {
        this.bookingTableService.deleteTable(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/bookingTables/{id}")
    public ResponseEntity<BookingTable> handleGetTable(@PathVariable("id") Long id) {
        BookingTable bookingTable = this.bookingTableService.getTableById(id);
        return ResponseEntity.status(HttpStatus.OK).body(bookingTable);
    }

    @GetMapping("/bookingTables/search")
    public ResponseEntity<BookingTable> handleGetTableName(
            @RequestParam(name = "tableName", required = false) String tableName) {
        BookingTable bookingTable = this.bookingTableService.getTableByName(tableName);
        return ResponseEntity.status(HttpStatus.OK).body(bookingTable);
    }

    @GetMapping("bookingTables")
    public ResponseEntity<ResultPaginationDTO> handleGellAllTables(
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "5", required = false) Integer size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {

        ResultPaginationDTO bookingTables = this.bookingTableService.getAllTables(page, size, keyword, status);
        return ResponseEntity.status(HttpStatus.OK).body(bookingTables);
    }

}
