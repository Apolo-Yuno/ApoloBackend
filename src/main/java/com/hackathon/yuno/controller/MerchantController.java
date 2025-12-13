package com.hackathon.yuno.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.service.MerchantService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/merchant")
@RestController
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/ingest")
    public ResponseEntity<MerchantResponseDTO> ingestData(@RequestBody IngestRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.ingestData(request));
    }
} 