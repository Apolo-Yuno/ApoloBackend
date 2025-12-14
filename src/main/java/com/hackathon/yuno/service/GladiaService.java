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

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GladiaService {

    private final RestTemplate restTemplate;

    @Value("${gladia.api.key}")
    private String gladiaApiKey;

    private static final String GLADIA_API_URL = "https://api.gladia.io/v2/transcription";

    private static final int MAX_POLLING_ATTEMPTS = 60;

    private static final int POLLING_INTERVAL_SECONDS = 5;

    public String transcribeAudio(MultipartFile audioFile, String language) {
        try {
            log.info("Starting Gladia transcription for file: {}", audioFile.getOriginalFilename());

            String transcriptionId = uploadAudioFile(audioFile, language);
            String transcription = pollForTranscription(transcriptionId);

            log.info("Transcription completed. Length: {} characters", transcription.length());
            return transcription;
            
        } catch (Exception e) {
            log.error("Error transcribing audio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }

    private String uploadAudioFile(MultipartFile audioFile, String language) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("x-gladia-key", gladiaApiKey);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        });
        
        if (language != null && !language.isEmpty()) {
            body.add("language", language);
        }
        
        body.add("diarization", "false");
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        log.debug("Uploading audio to Gladia...");
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            GLADIA_API_URL, requestEntity, 
            (Class<Map<String, Object>>)(Class<?>)Map.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String transcriptionId = (String) response.getBody().get("id");
            log.info("Audio uploaded. Transcription ID: {}", transcriptionId);
            return transcriptionId;
        }
        
        throw new RuntimeException("Failed to upload audio to Gladia");
    }

    private String pollForTranscription(String transcriptionId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-gladia-key", gladiaApiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        String statusUrl = GLADIA_API_URL + "/" + transcriptionId;
        
        for (int attempt = 0; attempt < MAX_POLLING_ATTEMPTS; attempt++) {
            log.debug("Polling transcription status (attempt {}/{})", attempt + 1, MAX_POLLING_ATTEMPTS);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                statusUrl, 
                HttpMethod.GET, 
                requestEntity,
                (Class<Map<String, Object>>)(Class<?>)Map.class
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) throw new RuntimeException("Null response from Gladia");
            
            String status = (String) body.get("status");
            log.debug("Transcription status: {}", status);
            
            if ("done".equalsIgnoreCase(status)) {
                return extractTranscription(body);
            } else if ("error".equalsIgnoreCase(status)) {
                throw new RuntimeException("Gladia transcription failed");
            }
            
            TimeUnit.SECONDS.sleep(POLLING_INTERVAL_SECONDS);
        }
        
        throw new RuntimeException("Transcription timeout");
    }

    @SuppressWarnings("unchecked")
    private String extractTranscription(Map<String, Object> response) {
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        if (result == null) throw new RuntimeException("No transcription result");
        
        String transcription = (String) result.get("transcription");
        
        if (transcription == null || transcription.isEmpty()) {
            throw new RuntimeException("Empty transcription result");
        }
        
        return transcription.trim();
    }
}
