package com.pluxity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class TestControllerBTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TestControllerB testControllerB;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(testControllerB)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void test() throws Exception {
        mockMvc.perform(get("/testB/test"))
                .andExpect(status().isOk())
                .andDo(document("project-b/test",
                        responseFields(
                                fieldWithPath("name").description("필드3 설명"),
                                fieldWithPath("description").description("필드4 설명")
                        )
                ));
    }
}