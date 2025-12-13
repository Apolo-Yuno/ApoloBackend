package com.hackathon.yuno.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import com.hackathon.yuno.model.enums.InteractionType;
import com.hackathon.yuno.model.enums.PaymentMethod;

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