package com.pluxity.feature.service;

import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeatureFacade {

    private final FeatureRepository featureRepository;

    public Feature findById(String id) {
        return featureRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FEATURE, id));
    }
}
