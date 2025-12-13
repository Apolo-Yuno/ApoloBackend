package com.hackathon.yuno.model.dto.response;

import com.hackathon.yuno.model.entity.MerchantContext; 
import com.hackathon.yuno.model.enums.LifeCicleState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestResponseDTO {
    private String id;
    private String name;
    private LifeCicleState lifeCicleState;
    private MerchantContext merchantContext; 
}