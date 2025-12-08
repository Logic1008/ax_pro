package com.axiang.ax_pro.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.axiang.ax_pro.service.StatsService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
/**
 * 控制器集成测试
 * 验证匿名访问、登录提交与结果查询，以及未登录提交的 401 行为
 */
public class TestControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    /** 列表与详情匿名可访问 */
    @Test
    void listAndDetailArePublic() throws Exception {
        mockMvc.perform(get("/api/tests")).andExpect(status().isOk());
        mockMvc.perform(get("/api/tests/1")).andExpect(status().isOk());
    }

    /** 未登录提交应返回 401 */
    @Test
    void submitRequiresLogin() throws Exception {
        String body = "{\"answers\":{\"101\":1001,\"102\":2001}}";
        mockMvc.perform(post("/api/tests/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    /** 登录后提交并返回结果代码 */
    @Test
    void loginThenSubmitWorks() throws Exception {
        String loginBody = "{\"username\":\"test\",\"password\":\"123456\"}";
        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(token.contains("token"));

        String body = "{\"answers\":{\"101\":1001,\"102\":2001}}";
        mockMvc.perform(post("/api/tests/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("token", token.replaceAll(".*\"token\":\"(.*?)\".*", "$1"))
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resultCode").exists());
    }

    /** 结果查询匿名可访问 */
    @Test
    void resultIsPublic() throws Exception {
        mockMvc.perform(get("/api/tests/1/result/E_S")).andExpect(status().isOk());
    }
}
