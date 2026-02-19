package ru.volkov.report.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.volkov.report.application.query.ListReportQuery;
import ru.volkov.report.application.readmodel.ReportMartReadModel;
import ru.volkov.report.dto.ReportDto;
import ru.volkov.report.dto.ReportListRequestDto;
import ru.volkov.report.dto.ReportListResponseDto;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ReportApiMapper {

    public abstract List<ReportDto> toDtoList(List<ReportMartReadModel> list);

    public abstract ListReportQuery toListReportQuery(ReportListRequestDto request);

    public ReportListResponseDto toListResponse(List<ReportMartReadModel> result) {
        return new ReportListResponseDto(
                toDtoList(result)
        );
    }
}