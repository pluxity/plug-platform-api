package com.pluxity.building.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.building.dto.BuildingResponseDto;
import com.pluxity.building.service.BuildingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class BuildingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BuildingService buildingService;

    @InjectMocks
    private BuildingController buildingController;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(buildingController)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void testGetBuilding() throws Exception {
        // Given
        BuildingResponseDto buildingResponseDto = new BuildingResponseDto("Building Name", "B001", "Seoul, Korea");
        when(buildingService.getBuilding()).thenReturn(buildingResponseDto);

        System.out.println("Expected Response: " + objectMapper.writeValueAsString(buildingResponseDto));

        // When & Then
        MvcResult result = mockMvc.perform(get("/building")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("building",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Building")
                                .summary("건물 정보 조회")
                                .description("건물 정보를 조회하는 API")
                                .responseSchema(Schema.schema("BuildingResponseDto"))
                                .responseFields(
                                        fieldWithPath("status").description("응답 상태").type(JsonFieldType.STRING),
                                        fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING),
                                        fieldWithPath("timestamp").description("응답 시간").type(JsonFieldType.STRING),
                                        fieldWithPath("result").description("응답 결과").type(JsonFieldType.OBJECT),
                                        fieldWithPath("result.name").description("건물 이름").type(JsonFieldType.STRING),
                                        fieldWithPath("result.code").description("건물 코드").type(JsonFieldType.STRING),
                                        fieldWithPath("result.address").description("건물 주소").type(JsonFieldType.STRING)
                                )
                                .build())))
                .andReturn();

        System.out.println("Actual Response: " + result.getResponse().getContentAsString());
    }
}