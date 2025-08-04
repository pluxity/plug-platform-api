package com.pluxity.asset.service;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_ASSET;

import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetValidator {
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public void validateAssetId(Long id) {
        assetRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_ASSET, id));
    }
}
