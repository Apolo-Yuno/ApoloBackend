package com.hackathon.yuno.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GladiaService {

    private final RestTemplate restTemplate;

    @Value("${gladia.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.gladia.io/v2";

    public String transcribeAudio(MultipartFile file) throws Exception {
        // 1. Subir Archivo
        String audioUrl = uploadFile(file);
        log.info("Audio subido a Gladia: {}", audioUrl);

        // 2. Iniciar Transcripción
        String transcriptionId = startTranscription(audioUrl);
        log.info("Transcripción iniciada con ID: {}", transcriptionId);

        // 3. Esperar Resultado (Polling)
        return pollForResults(transcriptionId);
    }

    // --- PASO 1: SUBIR ARCHIVO ---
    private String uploadFile(MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-gladia-key", apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Convertimos el MultipartFile a un recurso que RestTemplate entienda
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(BASE_URL + "/upload", requestEntity, Map.class);
        return (String) response.get("audio_url");
    }

    // --- PASO 2: INICIAR TRANSCRIPCIÓN ---
    private String startTranscription(String audioUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-gladia-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Configuramos Diarización (Detectar hablantes)
        Map<String, Object> body = Map.of(
                "audio_url", audioUrl,
                "diarization", true // ¡Importante para saber quién es el cliente!
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        Map response = restTemplate.postForObject(BASE_URL + "/transcription", requestEntity, Map.class);
        return (String) response.get("id");
    }

    // --- PASO 3: POLLING (ESPERAR) ---
    private String pollForResults(String id) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-gladia-key", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String status = "queued";
        Map response = null;

        // Intentamos por 60 segundos máx (Hackathon mode)
        for (int i = 0; i < 30; i++) {
            Thread.sleep(2000); // Esperar 2 segundos

            response = restTemplate.exchange(
                    BASE_URL + "/transcription/" + id,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class).getBody();

            status = (String) response.get("status");
            log.info("Estado transcripción: {}", status);

            if ("done".equals(status)) {
                break;
            }
        }

        if (!"done".equals(status)) {
            throw new RuntimeException("Tiempo de espera agotado para transcripción");
        }

        // Extraer el texto completo
        Map result = (Map) response.get("result");
        Map transcription = (Map) result.get("transcription");
        return (String) transcription.get("full_transcript");
    }
}