package com.thanh.foodorder.core.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.thanh.foodorder.core.response.ResultPaginationDTO;

public class UtilMethod {

    public static <T> ResultPaginationDTO toPagination(Pageable pageable, Page<T> page) {
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1); // get current page number
        meta.setPageSize(pageable.getPageSize()); // get page-size
        meta.setPages(page.getTotalPages()); // get total pages
        meta.setTotalElements(page.getTotalElements()); // get total elements in database

        resultPaginationDTO.setMeta(meta);
        return resultPaginationDTO;
    }
}
