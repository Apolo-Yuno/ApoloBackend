package com.hackathon.yuno.model.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hackathon.yuno.model.enums.InteractionType;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interactions")
public class Interaction {

    @Id
    private String id;
    private String merchantId;
    private String context;
    private LocalDateTime interactionDate;

    private InteractionType interactionType;

}