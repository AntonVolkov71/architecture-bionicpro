package ru.volkov.report.application.service;

import org.springframework.stereotype.Service;
import ru.volkov.report.application.query.ListReportQuery;
import ru.volkov.report.application.readmodel.ReportReadModel;
import ru.volkov.report.infrastructure.persistence.entity.ReportEntity;
import ru.volkov.report.infrastructure.persistence.mapper.ReportReadModelMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportApplicationService {

    private final ReportReadModelMapper readModelMapper;

    public ReportApplicationService(
            ReportReadModelMapper readModelMapper
    ) {
        this.readModelMapper = readModelMapper;
    }

    public List<ReportReadModel> getReports(ListReportQuery query) {
        System.out.println(query.getQ());

        List<ReportEntity> entities = new ArrayList<>();

        return readModelMapper.toReadModelList(entities);
    }
}
