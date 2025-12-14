package com.hackathon.yuno.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.yuno.mapper.MerchantMapper;
import com.hackathon.yuno.model.dto.ai.AIAnalysisResult;
import com.hackathon.yuno.model.dto.request.IngestRequestDTO;
import com.hackathon.yuno.model.dto.response.MerchantResponseDTO;
import com.hackathon.yuno.model.entity.Interaction;
import com.hackathon.yuno.model.entity.Merchant;
import com.hackathon.yuno.model.entity.MerchantContext;
import com.hackathon.yuno.model.entity.RiskProfile;
import com.hackathon.yuno.model.enums.LifeCicleState;
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

    @Transactional
    public MerchantResponseDTO ingestData(IngestRequestDTO request) {

        Interaction interaction = saveInteraction(request);

        AIAnalysisResult aiResult = aiService.analyze(request.getContent());

        String merchantName = resolveMerchantName(request, aiResult);

        Merchant merchant = merchantRepository.findByName(merchantName)
                .orElseGet(() -> createNewMerchant(merchantName));

        updateMerchantContext(merchant, aiResult);

        if (aiResult.getState() != null) {
            merchant.setLifeCicleState(aiResult.getState());
        }

        Merchant savedMerchant = merchantRepository.save(merchant);

        interaction.setMerchantId(savedMerchant.getId());
        interactionRepository.save(interaction);

        return merchantMapper.toDto(savedMerchant);
    }

    private Interaction saveInteraction(IngestRequestDTO request) {
        Interaction interaction = Interaction.builder()
                .context(request.getContent())
                .interactionType(request.getType())
                .interactionDate(LocalDateTime.now())
                .build();
        return interactionRepository.save(interaction);
    }

    private String resolveMerchantName(IngestRequestDTO request, AIAnalysisResult aiResult) {
        if (request.getMerchantName() != null && !request.getMerchantName().isEmpty()) {
            return request.getMerchantName();
        }
        return aiResult.getName() != null ? aiResult.getName() : "Unknown Merchant";
    }

    private Merchant createNewMerchant(String name) {
        return Merchant.builder()
                .name(name)
                .lifeCicleState(LifeCicleState.SALES)
                .merchantContext(MerchantContext.builder()
                        .countries(new ArrayList<>())
                        .providers(new ArrayList<>())
                        .paymentMethods(new ArrayList<>())
                        .riskData(new RiskProfile())
                        .build())
                .build();
    }

    private void updateMerchantContext(Merchant merchant, AIAnalysisResult aiData) {
        if (merchant.getMerchantContext() == null) {
            merchant.setMerchantContext(new MerchantContext());
        }
        MerchantContext currentCtx = merchant.getMerchantContext();
        MerchantContext aiCtx = aiData.getContext();

        if (aiCtx == null)
            return;

        mergeLists(currentCtx.getCountries(), aiCtx.getCountries());
        mergeLists(currentCtx.getProviders(), aiCtx.getProviders());
        mergeLists(currentCtx.getPaymentMethods(), aiCtx.getPaymentMethods());
        mergeRiskData(currentCtx, aiCtx.getRiskData());

        if (aiData.getSummary() != null) {
            currentCtx.setLastSummary(aiData.getSummary());
        }
    }

    /**
     * Método Genérico para mezclar listas sin duplicados.
     * Evita escribir el mismo bucle for 3 veces.
     */
    private <T> void mergeLists(List<T> currentList, List<T> newList) {
        if (newList == null || newList.isEmpty())
            return;

        if (currentList == null) {
            return;
        }

        for (T item : newList) {
            if (!currentList.contains(item)) {
                currentList.add(item);
            }
        }
    }

    /**
     * SOLUCIÓN DEL BUG: Mezcla inteligente de RiskData.
     * Solo sobrescribe si el dato nuevo existe.
     */
    private void mergeRiskData(MerchantContext currentCtx, RiskProfile newRisk) {
        if (newRisk == null)
            return;

        if (currentCtx.getRiskData() == null) {
            currentCtx.setRiskData(new RiskProfile());
        }
        RiskProfile currentRisk = currentCtx.getRiskData();

        if (newRisk.getRequestedLimit() != null) {
            currentRisk.setRequestedLimit(newRisk.getRequestedLimit());
        }

        if (newRisk.getCurrency() != null) {
            currentRisk.setCurrency(newRisk.getCurrency());
        }

        if (newRisk.getNotes() != null) {
            String oldNotes = currentRisk.getNotes() != null ? currentRisk.getNotes() : "";
            if (!oldNotes.contains(newRisk.getNotes())) {
                String separator = oldNotes.isEmpty() ? "" : " | ";
                currentRisk.setNotes(oldNotes + separator + newRisk.getNotes());
            }
        }
    }
}