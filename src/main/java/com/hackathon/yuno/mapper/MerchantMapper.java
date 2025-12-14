package com.hackathon.yuno.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.entity.Merchant;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    
    MerchantResponseDTO toDto(Merchant merchant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lifeCicleState", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "merchantContext", ignore = true)
    Merchant toEntity(IngestRequestDTO merchantRequest);

    

}
