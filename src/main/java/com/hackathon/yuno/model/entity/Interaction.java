package com.hackathon.yuno.model.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hackathon.yuno.model.enums.InteractionType;

import lombok.Data;

@Data
@Document(collection = "interactions")
public class Interaction {

    @Id
    private String id;
    private String merchantId;
    private String context;
    private LocalDateTime interactionDate;

    private InteractionType interactionType;

}