package com.openjob.admin.dto;

import com.openjob.common.model.HR;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class HrPaginationDTO {
    private Collection<HR> hrs;
    private Integer totalPages;
    private Long totalElements;

    public HrPaginationDTO(List<HR> content, int totalPages, long totalElements) {
        this.hrs = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
