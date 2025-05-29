package com.pluxity.file.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record FileResponse(
        Long id,
        String url,
        String originalFileName,
        String contentType,
        String fileStatus,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static FileResponse from(FileEntity fileEntity) {
        return new FileResponse(
                fileEntity.getId(),
                fileEntity.getFilePath(),
                fileEntity.getOriginalFileName(),
                fileEntity.getContentType(),
                fileEntity.getFileStatus().toString(),
                BaseResponse.of(fileEntity));
    }

    public static FileResponse empty() {
        return new FileResponse(null, null, null, null, null, null);
    }
}
