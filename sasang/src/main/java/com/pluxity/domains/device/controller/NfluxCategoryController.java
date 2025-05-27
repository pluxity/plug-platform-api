package com.pluxity.domains.device.controller;

import com.pluxity.domains.device.dto.NfluxCategoryCreateRequest;
import com.pluxity.domains.device.dto.NfluxCategoryResponse;
import com.pluxity.domains.device.dto.NfluxCategoryUpdateRequest;
import com.pluxity.domains.device.service.NfluxCategoryService;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nflux-categories")
@RequiredArgsConstructor
public class NfluxCategoryController {

    private final NfluxCategoryService nfluxCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataResponseBody<Long> create(@Valid @RequestBody NfluxCategoryCreateRequest request) {
        Long id = nfluxCategoryService.save(request);
        return DataResponseBody.of(HttpStatus.CREATED, "NFLux 카테고리가 생성되었습니다.", id);
    }

    @GetMapping
    public DataResponseBody<List<NfluxCategoryResponse>> findAll() {
        List<NfluxCategoryResponse> categories = nfluxCategoryService.findAll();
        return DataResponseBody.of(categories);
    }

    @GetMapping("/roots")
    public DataResponseBody<List<NfluxCategoryResponse>> findAllRoots() {
        List<NfluxCategoryResponse> categories = nfluxCategoryService.findAllRoots();
        return DataResponseBody.of(categories);
    }

    @GetMapping("/{id}")
    public DataResponseBody<NfluxCategoryResponse> findById(@PathVariable Long id) {
        NfluxCategoryResponse category = nfluxCategoryService.findById(id);
        return DataResponseBody.of(category);
    }

    @PutMapping("/{id}")
    public DataResponseBody<NfluxCategoryResponse> update(
            @PathVariable Long id, @Valid @RequestBody NfluxCategoryUpdateRequest request) {
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, request);
        return DataResponseBody.of(HttpStatus.OK, "NFlux 카테고리가 업데이트되었습니다.", updatedCategory);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseBody delete(@PathVariable Long id) {
        nfluxCategoryService.delete(id);
        return ResponseBody.of(HttpStatus.NO_CONTENT, "NFlux 카테고리가 삭제되었습니다.");
    }
}
