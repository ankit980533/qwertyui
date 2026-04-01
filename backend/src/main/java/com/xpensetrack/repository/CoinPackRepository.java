package com.xpensetrack.repository;

import com.xpensetrack.model.CoinPack;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoinPackRepository extends MongoRepository<CoinPack, String> {
}
