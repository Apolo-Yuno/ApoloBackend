package com.hackathon.yuno.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.yuno.mapper.MerchantMapper;
import com.hackathon.yuno.model.dto.ai.AIAnalysisResult;
import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.entity.Interaction;
import com.hackathon.yuno.model.entity.Merchant;
import com.hackathon.yuno.model.entity.MerchantContext;
import com.hackathon.yuno.model.enums.InteractionType;
import com.hackathon.yuno.model.enums.LifeCicleState;
import com.hackathon.yuno.model.enums.PaymentMethod;
import com.hackathon.yuno.repository.InteractionRepository;
import com.hackathon.yuno.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final AIService aiService;
    private final InteractionRepository interactionRepository;
    private final DocumentAnalizerService documentAnalizerService;

    @Transactional
    public MerchantResponseDTO ingestData(IngestRequestDTO request) {

        Interaction interaction = Interaction.builder()
        .context(request.getContent())
        .interactionType(request.getType())
        .interactionDate(LocalDateTime.now())
        .build();
        interaction = interactionRepository.save(interaction);

        AIAnalysisResult aiResult = aiService.analyze(request.getContent());

        String merchantName;

        if (request.getMerchantName() != null && !request.getMerchantName().isEmpty()) {
            merchantName = request.getMerchantName();
        } else {
            merchantName = aiResult.getName() != null ? aiResult.getName() : "Unknown Merchant";
        }

        String finalMerchantName = merchantName;
        Merchant merchant = merchantRepository.findByName(finalMerchantName)
            .orElseGet(() -> createNewMerchant(finalMerchantName));

        updateMerchantContext(merchant, aiResult);
        
        if (aiResult.getState() != null) {
            merchant.setLifeCicleState(aiResult.getState());
        }

        Merchant savedMerchant = merchantRepository.save(merchant);
        
        interaction.setMerchantId(savedMerchant.getId());
        interactionRepository.save(interaction);

        return merchantMapper.toDto(savedMerchant);
    }


    private Merchant createNewMerchant(String name) {
        Merchant newMerchant = Merchant.builder()
        .name(name)
        .lifeCicleState(LifeCicleState.SALES)
        .merchantContext(MerchantContext.builder()
            .countries(new ArrayList<>())
            .providers(new ArrayList<>())
            .paymentMethods(new ArrayList<>())
            .build())   
        .build();
        
        return newMerchant;
    }

    private void updateMerchantContext(Merchant merchant, AIAnalysisResult aiData) {
        MerchantContext context = merchant.getMerchantContext();
        if (context == null) { 
             context = new MerchantContext(); 
             merchant.setMerchantContext(context);
        }
        
        if (aiData.getContext() != null) {
            MerchantContext aiContext = aiData.getContext();
            
            if (aiContext.getCountries() != null) {
                if (context.getCountries() == null) {
                    context.setCountries(new ArrayList<>());
                }
                for (String c : aiContext.getCountries()) {
                    if (!context.getCountries().contains(c)) context.getCountries().add(c);
                }
            }

            if (aiContext.getProviders() != null) {
                if (context.getProviders() == null) {
                    context.setProviders(new ArrayList<>());
                }
                for (String p : aiContext.getProviders()) {
                    if (!context.getProviders().contains(p)) context.getProviders().add(p);
                }
            }
            
            if (aiContext.getPaymentMethods() != null) {
                if (context.getPaymentMethods() == null) {
                    context.setPaymentMethods(new ArrayList<>());
                }
                for (PaymentMethod pm : aiContext.getPaymentMethods()) {
                    if (!context.getPaymentMethods().contains(pm)) {
                        context.getPaymentMethods().add(pm);
                    }
                }
            }
            
            if (aiContext.getRiskNotes() != null) {
                context.setRiskNotes(aiContext.getRiskNotes());
            }
        }
        
        if (aiData.getSummary() != null) {
            context.setLastSummary(aiData.getSummary());
        }
    }

    @Transactional
    public MerchantResponseDTO proccessFromEmail(String senderEmail, String emailBody, InputStream attachmentStream){
        
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("EMAIL SENDER: ").append(senderEmail).append("\n");
        fullContent.append("EMAIL BODY: \n").append(emailBody).append("\n");

        if(attachmentStream != null){
            String pdfText = documentAnalizerService.extractTextFromPDFStream(attachmentStream);
            if(!pdfText.isEmpty()){
                fullContent.append("\nATTACHMENT CONTENT: \n").append(pdfText);
            }
        }

        IngestRequestDTO request = IngestRequestDTO.builder()
            .content(fullContent.toString())
            .type(InteractionType.EMAIL)
            .merchantName(null)
            .build();

        return ingestData(request);
    }
}