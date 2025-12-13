package com.hackathon.yuno.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;

@RequestMapping ("/merchant")
@RestController
public class MerchantController {

    @PostMapping("")
    public MerchantResponseDTO createMerchant(){
        return null;

    }



}
