package ru.volkov.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;

}
