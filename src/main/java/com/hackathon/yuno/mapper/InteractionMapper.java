package com.hackathon.yuno.mapper;

import org.mapstruct.Mapper;
import com.hackathon.yuno.model.entity.Interaction;
import com.hackathon.yuno.model.dto.response.InteractionResponseDTO;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InteractionMapper {
    
    List<InteractionResponseDTO> toDtoList(List<Interaction> interaction); 

}
