package com.hackathon.yuno.model.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class RiskProfile {

    private BigDecimal requestedLimit;
    private String currency;
    private String notes;

}
