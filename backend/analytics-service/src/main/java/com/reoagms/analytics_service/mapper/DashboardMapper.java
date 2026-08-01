package com.reoagms.analytics_service.mapper;

import com.reoagms.analytics_service.dto.DashboardRequest;
import com.reoagms.analytics_service.dto.DashboardResponse;
import com.reoagms.analytics_service.model.Dashboard;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public Dashboard toEntity(DashboardRequest request) {
        Dashboard entity = new Dashboard();
        update(entity, request);
        return entity;
    }

    public void update(Dashboard entity, DashboardRequest request) {
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setDashboardType(request.getDashboardType());
        entity.setOwnerUserId(request.getOwnerUserId());
        entity.setDefaultRangeDays(request.getDefaultRangeDays());
        entity.setActive(request.getActive());
        entity.setLayoutConfiguration(request.getLayoutConfiguration());
    }

    public DashboardResponse toResponse(Dashboard entity) {
        return DashboardResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .dashboardType(entity.getDashboardType())
                .ownerUserId(entity.getOwnerUserId())
                .defaultRangeDays(entity.getDefaultRangeDays())
                .active(entity.getActive())
                .layoutConfiguration(entity.getLayoutConfiguration())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
