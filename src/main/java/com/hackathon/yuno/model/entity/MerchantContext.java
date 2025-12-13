package com.hackathon.yuno.model.entity;

import java.util.List;

import com.hackathon.yuno.model.enums.PaymentMethod;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MerchantContext {

    private List<String> countries;
    private List<PaymentMethod> paymentMethods;
    private List<String> providers;
    private String riskNotes;
    private String lastSummary;

}
