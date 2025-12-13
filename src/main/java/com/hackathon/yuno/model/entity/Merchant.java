package com.hackathon.yuno.model.entity;

import com.hackathon.yuno.model.enums.LifeCicleState;
import com.hackathon.yuno.model.enums.PaymentMethod;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
@Document(collection = "merchants")
public class Merchant {

    @Id
    private String id;
    private String name;
    private LifeCicleState lifeCicleState;

    private MerchantContext merchantContext;

}
