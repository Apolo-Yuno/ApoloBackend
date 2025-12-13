package com.hackathon.yuno.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.hackathon.yuno.model.dto.ai.AIAnalysisResult;
import com.hackathon.yuno.model.entity.MerchantContext;
import com.hackathon.yuno.model.enums.PaymentMethod;
import com.hackathon.yuno.model.enums.LifeCicleState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Value("${openai.api.key}")
    private String apiKey;    

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";


    public AIAnalysisResult analyze(String rawText) {
        System.out.println("========== ANALYZING TEXT ==========");
        System.out.println("Input text: " + rawText);
        System.out.println("API Key configured: " + (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here")));
        System.out.println("====================================");
        try {
            return callGroq(rawText);
        } catch (Exception e) {
            System.err.println("ERROR calling Groq: " + e.getMessage());
            e.printStackTrace();
            return new AIAnalysisResult(); 
        }
    }

    @SuppressWarnings("unchecked")
    private AIAnalysisResult callGroq(String text) throws Exception {
        String prompt = String.format("""
            Extrae información del siguiente mensaje de un merchant de pagos.
            
            MENSAJE: "%s"
            
            Responde SOLO con este JSON (sin explicaciones, sin markdown):
            {
              "name": "nombre del merchant o Unknown Merchant si no hay",
              "state": "uno de: SALES, CONTRACT, IMPLEMENTATION, LIVE",
              "context": {
                "countries": ["códigos ISO como CO, MX, etc o array vacío"],
                "paymentMethods": ["uno o varios de: PSE, CREDITCARD, DEBITCARD, CASH o array vacío"],
                "providers": ["STRIPE, ADYEN, etc en mayúsculas o array vacío"],
                "riskNotes": "notas sobre límites/riesgo o null"
              },
              "summary": "resumen breve del mensaje"
            }
            
            Reglas:
            - state: SALES=interés inicial, CONTRACT=firma/legal/contrato, IMPLEMENTATION=integración técnica, LIVE=producción
            - Si no hay nombre de merchant, usa "Unknown Merchant"
            - Arrays vacíos si no hay info: []
            - riskNotes null si no hay info de riesgo
            - SIEMPRE incluye todas las claves
            
            Ejemplo para "Hola somos Zoop, queremos PSE en Colombia":
            {"name":"Zoop","state":"SALES","context":{"countries":["CO"],"paymentMethods":["PSE"],"providers":[],"riskNotes":null},"summary":"Zoop interesado en PSE Colombia"}
            """, text);
            Map<String, Object> requestBody = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of("role", "system", "content", "Eres un asistente experto en análisis de datos de merchants de pagos. Respondes ÚNICAMENTE con JSON válido, sin markdown ni explicaciones adicionales."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.0
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        var response = restTemplate.postForEntity(GROQ_URL, entity, Map.class);

    log.info("Groq response status: {}", response.getStatusCodeValue());
    Map<String, Object> body = response.getBody();
    log.debug("Groq raw body object: {}", body);
        if (body == null) {
            log.error("Groq response body is null");
            return new AIAnalysisResult();
        }
        
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("Groq response has no choices");
            return new AIAnalysisResult();
        }
        
        Object firstChoice = choices.get(0);
        String jsonString = null;
        if (firstChoice instanceof Map) {
            Map<String, Object> fc = (Map<String, Object>) firstChoice;
            Object messageObj = fc.get("message");
            if (messageObj instanceof Map) {
                jsonString = (String) ((Map<String, Object>) messageObj).get("content");
            }
            if (jsonString == null) {
                jsonString = (String) fc.get("content");
            }
            if (jsonString == null) {
                jsonString = (String) fc.get("text");
            }
        }
        if (jsonString == null) {
            log.error("Could not extract content from Groq response choices: {}", choices.get(0));
            return new AIAnalysisResult();
        }
        
        System.out.println("========== RAW GROQ RESPONSE ==========");
        System.out.println(jsonString);
        System.out.println("=======================================");
        
        jsonString = jsonString.replace("```json", "").replace("```", "").trim();
        
        System.out.println("========== CLEANED JSON ==========");
        System.out.println(jsonString);
        System.out.println("==================================");
        
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        AIAnalysisResult result = null;
        try {
            result = objectMapper.readValue(jsonString, AIAnalysisResult.class);
        } catch (Exception e) {
            log.warn("Direct binding to AIAnalysisResult failed, attempting fallback parsing: {}", e.toString());
        }

        if (result == null || result.getContext() == null || result.getName() == null) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(jsonString, Map.class);
                AIAnalysisResult fallback = new AIAnalysisResult();

                Object n = parsed.getOrDefault("name", parsed.get("merchantName"));
                fallback.setName(n != null ? n.toString() : null);

                Object s = parsed.getOrDefault("state", parsed.get("suggestedState"));
                if (s != null) {
                    try {
                        fallback.setState(LifeCicleState.valueOf(s.toString().toUpperCase()));
                    } catch (Exception ex) {
                    }
                }

                Object ctxObj = parsed.get("context");
                MerchantContext ctx = new MerchantContext();
                if (ctxObj instanceof Map) {
                    Map<String, Object> c = (Map<String, Object>) ctxObj;
                    Object countries = c.get("countries");
                    if (countries instanceof Iterable) {
                        for (Object co : (Iterable<?>) countries) {
                            if (co != null) ctx.getCountries().add(co.toString());
                        }
                    }

                    Object pms = c.get("paymentMethods");
                    if (pms instanceof Iterable) {
                        for (Object pm : (Iterable<?>) pms) {
                            if (pm == null) continue;
                            try {
                                PaymentMethod enumPm = PaymentMethod.valueOf(pm.toString().toUpperCase());
                                ctx.getPaymentMethods().add(enumPm);
                            } catch (Exception ex) {
                            }
                        }
                    }

                    Object providers = c.get("providers");
                    if (providers instanceof Iterable) {
                        for (Object pr : (Iterable<?>) providers) {
                            if (pr != null) ctx.getProviders().add(pr.toString());
                        }
                    }

                    Object risk = c.get("riskNotes");
                    if (risk != null) ctx.setRiskNotes(risk.toString());
                }

                fallback.setContext(ctx);

                Object summary = parsed.get("summary");
                if (summary != null) fallback.setSummary(summary.toString());

                result = fallback;
                log.info("Fallback parsed AI result: name={}, state={}, countries={}, pms={}, providers={}",
                        result.getName(), result.getState(), result.getContext().getCountries(), result.getContext().getPaymentMethods(), result.getContext().getProviders());
            } catch (Exception ex) {
                log.error("Fallback parsing failed: {}", ex.toString());
                if (result == null) result = new AIAnalysisResult();
            }
        }

        log.info("Final parsed result - Name: {}, State: {}, Context null: {}", result.getName(), result.getState(), result.getContext() == null);
        return result;
    }
}
