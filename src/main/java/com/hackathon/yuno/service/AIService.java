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
import com.hackathon.yuno.model.dto.ai.AIAnalysisResult;

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
        try {
            return callGroq(rawText);
        } catch (Exception e) {
            log.error("Error llamando a Groq: ", e);
            return new AIAnalysisResult(); 
        }
    }

    @SuppressWarnings("unchecked")
    private AIAnalysisResult callGroq(String text) throws Exception {
        String prompt = """
            Analiza el texto: "%s"
            
            Extrae los datos y responde SOLO un JSON válido con esta estructura exacta:
            {
              "name": "Nombre de la empresa o Unknown",
              "state": "ENUM", 
              "context": {
                "countries": ["ISO Code 2 letras (ej: CO, MX)"],
                "providers": ["Nombre en Mayusculas (ej: STRIPE)"],
                "paymentMethods": ["CARD", "PSE", "CASH"],
                "riskNotes": "Texto corto de riesgo o null",
                "lastSummary": "Resumen del contexto actual"
              },
              "summary": "Resumen muy breve de la interacción"
            }
            
            IMPORTANTE: Los paymentMethods deben ser strings válidos del enum PaymentMethod.
            
            REGLAS PARA EL ENUM 'state':
            - Si es ventas/interés -> usa "SALES"
            - Si es contrato/legal -> usa "CONTRACT"
            - Si es integración/pruebas -> usa "IMPLEMENTATION"
            - Si es producción/envivo -> usa "LIVE"
            """.formatted(text);

        Map<String, Object> requestBody = Map.of(
            "model", "llama3-70b-8192",
            "messages", List.of(
                Map.of("role", "system", "content", "Responde solo JSON raw, sin markdown."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.0
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        var response = restTemplate.postForEntity(GROQ_URL, entity, Map.class);

        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        String jsonString = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

        jsonString = jsonString.replace("```json", "").replace("```", "").trim();
        return objectMapper.readValue(jsonString, AIAnalysisResult.class);
    }

 
}
