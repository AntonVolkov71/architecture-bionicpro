package ru.volkov.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDto {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 500;
    public static final String DEFAULT_SORT = "updatedAt:desc";

    @Min(1)
    private Integer page;

    @Min(1)
    @Max(MAX_PAGE_SIZE)
    private Integer pageSize;

    @Pattern(
            regexp = "^[a-zA-Z0-9_]+:(asc|desc)$",
            message = "sort must be in format field:asc|desc"
    )
    private String sort;  // Сортировка, формат `field:asc, desc` (например `updatedAt:desc`)

    public Integer getPage() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public Integer getPageSize() {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public String getSort() {
        return sort == null ? DEFAULT_SORT : sort;
    }

}
