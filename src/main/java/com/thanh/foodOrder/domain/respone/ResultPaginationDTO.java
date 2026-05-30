package com.thanh.foodOrder.domain.respone;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {
    private Meta meta;
    private Object results;

    @Getter
    @Setter
    public static class Meta {

        private int page;
        private int pageSize;
        private int pages;
        private long totalElements;
    }

}
