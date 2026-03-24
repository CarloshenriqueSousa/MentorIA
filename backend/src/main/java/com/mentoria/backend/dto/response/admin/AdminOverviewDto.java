package com.mentoria.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminOverviewDto {

    private String environment;
    private List<AdminAgentDto> agents;
}
