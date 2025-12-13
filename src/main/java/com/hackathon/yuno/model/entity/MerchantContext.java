package com.hackathon.yuno.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hackathon.yuno.model.enums.PaymentMethod;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantContext {

    private List<String> countries = new ArrayList<>();
    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private List<String> providers = new ArrayList<>();
    private String riskNotes;
    private String lastSummary;

}
