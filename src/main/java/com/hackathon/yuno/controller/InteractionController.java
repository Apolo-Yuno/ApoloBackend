package com.hackathon.yuno.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.yuno.service.InteractionService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import com.hackathon.yuno.model.dto.response.InteractionResponseDTO;

@RequestMapping("/interaction")
@RestController
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/AllInteractions")
    public List<InteractionResponseDTO> getInteractionByMerchantId(@PathVariable String merchantId) {

        return interactionService.getAllInteractionsByMerchantId(merchantId);

    }

}
