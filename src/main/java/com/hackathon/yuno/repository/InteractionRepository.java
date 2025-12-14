package com.hackathon.yuno.repository;

import java.util.Optional;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hackathon.yuno.model.entity.Interaction;

public interface InteractionRepository extends MongoRepository<Interaction, String> {

    Optional<Interaction> findByMerchantId(String merchantId);

    List<Interaction> findAllByMerchantId(String merchantId);

}
