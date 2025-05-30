package com.pluxity.domains.device.dto;

import com.pluxity.file.dto.FileResponse;
import java.util.List;

public record NfluxCategoryGroupResponse(
        Long categoryId,
        String categoryName,
        String contextPath,
        FileResponse iconFile,
        List<NfluxResponse> devices) {}
