package com.hackathon.yuno.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hackathon.yuno.model.entity.Merchant;

public interface MerchantRepository extends MongoRepository<Merchant, String> {

    Optional<Merchant> findByName(String name);

}
