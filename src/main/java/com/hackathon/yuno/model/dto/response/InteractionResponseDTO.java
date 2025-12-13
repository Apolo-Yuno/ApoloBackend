package com.hackathon.yuno.model.dto.response;

import java.time.LocalDateTime;

import com.hackathon.yuno.model.enums.InteractionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponseDTO {

    private String context;
    private LocalDateTime interactionDate;
    private InteractionType interactionType;
}