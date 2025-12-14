package com.hackathon.yuno.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.enums.InteractionType;
import com.hackathon.yuno.service.MerchantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/merchant")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/ingest")
    public ResponseEntity<MerchantResponseDTO> ingestData(@RequestBody IngestRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.ingestData(request));
    }

    @PostMapping(value = "/ingest/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MerchantResponseDTO> ingestAudioData(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("merchantName") String merchantName,
            @RequestParam(value = "type", defaultValue = "CALL") InteractionType type,
            @RequestParam(value = "language", defaultValue = "es") String language) {
        
        log.info("Received audio ingestion request for merchant: {}", merchantName);
        
        if (audioFile.isEmpty()) {
            log.warn("Empty audio file received");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Processing audio file: {} (size: {} KB)", 
                audioFile.getOriginalFilename(), 
                audioFile.getSize() / 1024);
        
        MerchantResponseDTO response = merchantService.ingestAudioData(audioFile, merchantName, type, language);
        
        log.info("Audio processed successfully for merchant: {}", merchantName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 