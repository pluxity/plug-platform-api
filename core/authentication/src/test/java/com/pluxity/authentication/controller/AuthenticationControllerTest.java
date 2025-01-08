package com.pluxity.authentication.controller;

import static com.pluxity.global.constant.SuccessCode.SUCCESS_CREATE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pluxity.authentication.dto.SignUpRequestDto;
import com.pluxity.authentication.service.AuthenticationService;
import com.pluxity.user.constant.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock private AuthenticationService authenticationService;

    @InjectMocks private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        MockitoAnnotations.openMocks(this);
        mockMvc =
                MockMvcBuilders.standaloneSetup(authenticationController)
                        .apply(documentationConfiguration(restDocumentation))
                        .build();
    }

    @Test
    public void testSignUp() throws Exception {
        SignUpRequestDto signUpRequestDto =
                new SignUpRequestDto("admin", "password", "관리자", "1234", Role.ADMIN);

        mockMvc
                .perform(
                        post("/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SUCCESS_CREATE.getHttpStatus().name()))
                .andDo(document("sign-up"));
    }
}