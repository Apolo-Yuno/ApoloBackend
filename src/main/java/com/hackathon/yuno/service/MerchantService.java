package com.hackathon.yuno.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.yuno.exceptions.MerchantNotFound;
import com.hackathon.yuno.mapper.MerchantMapper;
import com.hackathon.yuno.model.dto.request.MerchantRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.entity.Merchant;
import com.hackathon.yuno.model.enums.LifeCicleState;
import com.hackathon.yuno.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;


    @Transactional
    public MerchantResponseDTO createMerchant(MerchantRequestDTO merchant){
        
        Merchant newMerchant = merchantMapper.toEntity(merchant);

        Merchant merchantToSave = merchantRepository.save(newMerchant);

        return merchantMapper.toDto(merchantToSave);

    }
    
    @Transactional
    public MerchantResponseDTO getMerchantContext(String id){

        Merchant merchant = merchantRepository.findById(id).orElseThrow(() -> new MerchantNotFound("The merchant with id: {id} not found"));
        
        return merchantMapper.toDto(merchant);

    }

    @Transactional
    public MerchantResponseDTO updateLifyCycleState(String id, LifeCicleState state){

        Merchant merchantToUpdate = merchantRepository.findById(id).orElseThrow(() -> new MerchantNotFound("Merchant with id: {id} not found "));

        merchantToUpdate.setLifeCicleState(state);

        return merchantMapper.toDto(merchantToUpdate);
        

    }

}
