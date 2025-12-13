package com.hackathon.yuno.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.yuno.repository.InteractionRepository;
import com.hackathon.yuno.model.entity.Interaction;
import com.hackathon.yuno.mapper.InteractionMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import com.hackathon.yuno.model.dto.response.InteractionResponseDTO;

@Service
@RequiredArgsConstructor
public class InteractionService {
    
    private final InteractionRepository interactionRepository;
    private final InteractionMapper interactionMapper;

    @Transactional
    public List<InteractionResponseDTO> getAllInteractionsByMerchantId(String merchantId){

        List<Interaction> interactions = interactionRepository.findAllByMerchantId(merchantId);

        return interactionMapper.toDtoList(interactions);
    }

}
