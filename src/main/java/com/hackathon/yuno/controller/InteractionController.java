package com.hackathon.yuno.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hackathon.yuno.service.InteractionService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.hackathon.yuno.model.dto.response.InteractionResponseDTO;

@RequestMapping("/interaction")
@RestController
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/{merchantId}")
    public ResponseEntity<List<InteractionResponseDTO>> getInteractionByMerchantId(@PathVariable String merchantId) {

        return ResponseEntity.ok(interactionService.getAllInteractionsByMerchantId(merchantId));

    }

}
