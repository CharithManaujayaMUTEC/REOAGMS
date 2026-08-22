package com.reoagms.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportCollectionResponse {
    private int totalReports;
    private List<ReportSummaryResponse> reports;
}
