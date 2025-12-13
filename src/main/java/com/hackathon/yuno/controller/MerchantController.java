package com.hackathon.yuno.controller;

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
    public MerchantResponseDTO ingestData(@RequestBody IngestRequestDTO request) {
        return merchantService.ingestData(request);
    }
    
}