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
        return String.format(
                """
                        Actúa como un Arquitecto de Soluciones de Pagos (Payment Gateway).
                        Tu tarea es extraer datos estructurados de mensajes informales.

                        MENSAJE A ANALIZAR: "%s"

                        INSTRUCCIONES DE EXTRACCIÓN:
                        1. Merchant Name: Identifica la EMPRESA, no la persona que escribe. Si dice "Soy Juan de Uber", el merchant es "Uber".
                        2. State (Ciclo de Vida):
                        - SALES: Preguntas comerciales, precios, "queremos saber", "interesados".
                        - CONTRACT: Temas legales, firma, documentos, compliance.
                        - IMPLEMENTATION: Credenciales, API Key, Sandbox, Dev, SDK, errores técnicos (404, 401).
                        - LIVE: Salida a producción, tráfico real, go-live.
                        *Regla*: Si hay conflicto, elige el estado más avanzado mencionado.
                        3. Payment Methods: Mapea a ENUMS exactos:
                        - "Tarjeta", "Visa", "Master" -> CREDITCARD
                        - "PSE", "Transferencia" -> PSE
                        - "Efectivo", "SuRed", "Baloto" -> CASH
                        4. Risk & Limits:
                        - Si mencionan montos (ej: "cupo de 50k", "límite de 20 millones"), extrae el número y la moneda.

                        FORMATO DE RESPUESTA JSON (Estricto):
                        {
                        "name": "String (Nombre de empresa o 'Unknown')",
                        "contactPerson": "String (Nombre de quien escribe o null)",
                        "state": "ENUM",
                        "context": {
                            "countries": ["ISO_CODE_2_CHAR"],
                            "paymentMethods": ["ENUM"],
                            "providers": ["STRING_UPPERCASE"],
                            "riskData": {
                            "requestedLimit": Number (o null),
                            "currency": "ISO_CODE_3_CHAR" (o null),
                            "notes": "String con el contexto de riesgo"
                            }
                        },
                        "summary": "Resumen ejecutivo corto"
                        }

                        IMPORTANTE: Responde SOLO el JSON. No uses markdown.
                        """,
                text);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "Eres un asistente experto en análisis de datos de merchants de pagos. Respondes ÚNICAMENTE con JSON válido, sin markdown ni explicaciones adicionales."),
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.0);
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
