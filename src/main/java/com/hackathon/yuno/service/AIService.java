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
        log.info("Analyzing text of length: {}", rawText != null ? rawText.length() : 0);
        try {
            return callGroq(rawText);
        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            return new AIAnalysisResult(); 
        }
    }

    private AIAnalysisResult callGroq(String text) throws Exception {
        String prompt = createPrompt(text);
        Map<String, Object> requestBody = buildRequestBody(prompt);
        HttpEntity<Map<String, Object>> entity = buildRequestEntity(requestBody);

        var response = restTemplate.postForEntity(GROQ_URL, entity, Map.class);
        log.info("Groq response status: {}", response.getStatusCodeValue());

        return parseResponse(response.getBody());
    }

    private String createPrompt(String text) {
        return String.format("""
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
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of("role", "system", "content", "Eres un asistente experto en análisis de datos de merchants de pagos. Respondes ÚNICAMENTE con JSON válido, sin markdown ni explicaciones adicionales."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.0
        );
    }

    private HttpEntity<Map<String, Object>> buildRequestEntity(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return new HttpEntity<>(requestBody, headers);
    }

    @SuppressWarnings("unchecked")
    private AIAnalysisResult parseResponse(Map<String, Object> body) {
        if (body == null) {
            log.warn("Groq response body is null");
            return new AIAnalysisResult();
        }

        String jsonString = extractContentFromChoices(body);
        if (jsonString == null) {
            return new AIAnalysisResult();
        }

        jsonString = jsonString.replace("```json", "").replace("```", "").trim();

        try {
            AIAnalysisResult result = objectMapper.readValue(jsonString, AIAnalysisResult.class);
            log.info("Parsed AI result successfully: {}", result.getName());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", e.getMessage());
            return new AIAnalysisResult();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContentFromChoices(Map<String, Object> body) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            log.warn("Groq response has no choices");
            return null;
        }
        
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        
        if (message != null) {
            return (String) message.get("content");
        }
        return null;
    }
}
