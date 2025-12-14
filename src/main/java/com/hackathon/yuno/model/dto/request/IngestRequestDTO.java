package com.hackathon.yuno.model.dto.request;

import com.hackathon.yuno.model.enums.InteractionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestRequestDTO {

    private String content;
    private InteractionType type;
    private String merchantName;
    
}