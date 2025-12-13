package com.hackathon.yuno.model.dto.response;

import com.hackathon.yuno.model.entity.Interaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hackathon.yuno.model.enums.InteractionType;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponseDTO {

    private String context;
    private LocalDateTime interactionDate;
    private InteractionType interactionType;
}