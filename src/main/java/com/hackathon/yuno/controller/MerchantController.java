package com.hackathon.yuno.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.enums.InteractionType;
import com.hackathon.yuno.service.GladiaService;
import com.hackathon.yuno.service.MerchantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequestMapping("/merchant")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final MerchantService merchantService;
    private final GladiaService gladiaService;

    @PostMapping("/ingest")
    public ResponseEntity<MerchantResponseDTO> ingestData(@RequestBody IngestRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.ingestData(request));
    }

    @PostMapping(value = "/upload-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MerchantResponseDTO uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            String transcript = gladiaService.transcribeAudio(file);

            IngestRequestDTO request = IngestRequestDTO.builder()
                    .content(transcript)
                    .type(InteractionType.CALL)
                    .merchantName(null)
                    .build();

            return merchantService.ingestData(request);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error procesando audio: " + e.getMessage());
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<MerchantResponseDTO> getMerchantByName(@PathVariable String name) {
        return ResponseEntity.ok(merchantService.getMerchantByName(name));
    }

    @GetMapping
    public ResponseEntity<List<MerchantResponseDTO>> getAllMerchants() {
        return ResponseEntity.ok(merchantService.getAllMerchants());
    }
}