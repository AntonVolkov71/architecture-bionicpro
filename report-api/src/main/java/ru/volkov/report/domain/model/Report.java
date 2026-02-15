package ru.volkov.report.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Report {
    private Long id;

    @NotBlank
    private String name;

}
