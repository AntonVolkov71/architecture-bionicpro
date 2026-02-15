package ru.volkov.report.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volkov.report.api.mapper.ReportApiMapper;
import ru.volkov.report.application.query.ListReportQuery;
import ru.volkov.report.application.readmodel.ReportReadModel;
import ru.volkov.report.application.service.ReportApplicationService;
import ru.volkov.report.dto.ReportListRequestDto;
import ru.volkov.report.dto.ReportListResponseDto;

import java.util.List;

@RestController
@RequestMapping("")
public class ReportController {

    private final ReportApplicationService reportApplicationService;
    private final ReportApiMapper reportApiMapper;

    public ReportController(ReportApplicationService reportApplicationService, ReportApiMapper reportApiMapper) {
        this.reportApplicationService = reportApplicationService;
        this.reportApiMapper = reportApiMapper;
    }

    @GetMapping("/reports")
    public ReportListResponseDto index() {
//        @Valid @ModelAttribute ReportListRequestDto req
        ReportListRequestDto reqMock = new ReportListRequestDto();

        ListReportQuery query = reportApiMapper.toListReportQuery(reqMock);
        List<ReportReadModel> result = reportApplicationService.getReports(query);

        return reportApiMapper.toListResponse(result);
    }
}
