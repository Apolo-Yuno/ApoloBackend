package com.hackathon.yuno.mapper;

import org.mapstruct.Mapper;

import com.hackathon.yuno.model.dto.request.MerchantRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.entity.Merchant;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    
    MerchantResponseDTO toDto(Merchant merchant);

    Merchant toEntity(MerchantRequestDTO merchantRequest);

    

}
