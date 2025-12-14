package com.hackathon.yuno.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hackathon.yuno.model.enums.LifeCicleState;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import lombok.Builder;
import lombok.AllArgsConstructor;

@Data
@Builder    
@RequiredArgsConstructor
@AllArgsConstructor
@Document(collection = "merchants")
public class Merchant {

    @Id
    private String id;
    private String name;
    private LifeCicleState lifeCicleState;
    private MerchantContext merchantContext;

}
