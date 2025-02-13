package com.pluxity.user.dto;

import com.pluxity.user.entity.Template;

public record TemplateResponse(Long id, String name, String thumbnail) {
    public static TemplateResponse from(Template template) {
        return new TemplateResponse(template.getId(), template.getName(), template.getThumbnail());
    }
}
