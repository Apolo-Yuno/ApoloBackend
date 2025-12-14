package com.hackathon.yuno.service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.yuno.exceptions.MerchantNotFound;
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
    private final GladiaService gladiaService;


    @Transactional
    public MerchantResponseDTO ingestAudioData(MultipartFile audioFile, String merchantName, InteractionType type, String language) {
        try {
            log.info("Processing audio for merchant: {}", merchantName);
            
            String transcription = gladiaService.transcribeAudio(audioFile, language);
            log.info("Audio transcribed successfully. Length: {} characters", transcription.length());
            
            IngestRequestDTO request = IngestRequestDTO.builder()
                .content(transcription)
                .type(type)
                .merchantName(merchantName)
                .build();
            
            return ingestData(request);
            
        } catch (Exception e) {
            log.error("Error processing audio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process audio", e);
        }
    }

    @Transactional
    public MerchantResponseDTO ingestData(IngestRequestDTO request) {

        Interaction interaction = new Interaction();
        interaction.setContext(request.getContent());
        interaction.setInteractionType(request.getType());
        interaction.setInteractionDate(LocalDateTime.now());
        interaction = interactionRepository.save(interaction);

        AIAnalysisResult aiResult = aiService.analyze(request.getContent());

        log.info("========== AI RESULT IN MERCHANT SERVICE ==========");
        log.info("AIResult Name: {}", aiResult.getName());
        log.info("AIResult State: {}", aiResult.getState());
        log.info("AIResult Context is null: {}", aiResult.getContext() == null);
        if (aiResult.getContext() != null) {
            log.info("AIResult Context Countries: {}", aiResult.getContext().getCountries());
            log.info("AIResult Context PaymentMethods: {}", aiResult.getContext().getPaymentMethods());
            log.info("AIResult Context Providers: {}", aiResult.getContext().getProviders());
        }
        log.info("===================================================");

        String merchantName;
        System.out.println(request.getMerchantName());
        System.out.println("FUNCIONAAAAA");
        System.out.println("FUNCIONAAAAAAAAA");

        

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
        Merchant m = new Merchant();
        m.setName(name);
        m.setLifeCicleState(LifeCicleState.SALES);
        m.setMerchantContext(new MerchantContext()); // Inicializar vacío
        m.getMerchantContext().setCountries(new ArrayList<>());
        m.getMerchantContext().setProviders(new ArrayList<>());
        m.getMerchantContext().setPaymentMethods(new ArrayList<>());
        return m;
    }

    private void updateMerchantContext(Merchant merchant, AIAnalysisResult aiData) {
        MerchantContext ctx = merchant.getMerchantContext();
        if (ctx == null) { 
             ctx = new MerchantContext(); 
             merchant.setMerchantContext(ctx);
        }
        
        if (aiData.getContext() != null) {
            MerchantContext aiContext = aiData.getContext();
            
            if (aiContext.getCountries() != null) {
                if (ctx.getCountries() == null) {
                    ctx.setCountries(new ArrayList<>());
                }
                for (String c : aiContext.getCountries()) {
                    if (!ctx.getCountries().contains(c)) ctx.getCountries().add(c);
                }
            }

            if (aiContext.getProviders() != null) {
                if (ctx.getProviders() == null) {
                    ctx.setProviders(new ArrayList<>());
                }
                for (String p : aiContext.getProviders()) {
                    if (!ctx.getProviders().contains(p)) ctx.getProviders().add(p);
                }
            }
            
            if (aiContext.getPaymentMethods() != null) {
                if (ctx.getPaymentMethods() == null) {
                    ctx.setPaymentMethods(new ArrayList<>());
                }
                for (PaymentMethod pm : aiContext.getPaymentMethods()) {
                    if (!ctx.getPaymentMethods().contains(pm)) {
                        ctx.getPaymentMethods().add(pm);
                    }
                }
            }
            
            if (aiContext.getRiskNotes() != null) {
                ctx.setRiskNotes(aiContext.getRiskNotes());
            }
        }
        
        if (aiData.getSummary() != null) {
            ctx.setLastSummary(aiData.getSummary());
        }
    }

    @Transactional
    public MerchantResponseDTO createMerchant(IngestRequestDTO merchant){
        
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