package com.hackathon.yuno.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hackathon.yuno.model.enums.PaymentMethod;
import com.hackathon.yuno.model.entity.RiskProfile;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantContext {

    private List<String> countries = new ArrayList<>();
    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private List<String> providers = new ArrayList<>();

    private RiskProfile riskData;
    private String lastSummary;

}
