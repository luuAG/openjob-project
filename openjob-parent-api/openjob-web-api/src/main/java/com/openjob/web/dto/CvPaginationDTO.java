package com.openjob.web.dto;

import com.openjob.common.model.CV;
import lombok.Getter;

import java.util.Collection;

@Getter
public class CvPaginationDTO {
    Collection<CvDTO> listCv;
    Integer totalPages;
    Long totalElements;

    public CvPaginationDTO(Collection<CvDTO> listCv, Integer totalPages, Long totalElements) {
        this.listCv = listCv;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
