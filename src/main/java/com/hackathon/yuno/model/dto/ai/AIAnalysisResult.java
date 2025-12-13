package com.hackathon.yuno.model.dto.ai;

import com.hackathon.yuno.model.entity.MerchantContext;
import com.hackathon.yuno.model.enums.LifeCicleState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIAnalysisResult {
    
    private String name;

    private LifeCicleState state;

    private MerchantContext context;

    private String summary;

}
