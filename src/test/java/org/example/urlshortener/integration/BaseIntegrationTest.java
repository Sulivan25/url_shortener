package org.example.urlshortener.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.urlshortener.auth.dto.AuthResponse;
import org.example.urlshortener.auth.dto.LoginRequest;
import org.example.urlshortener.auth.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base class for integration tests.
 *
 * <p>Boots the full Spring context with H2 (PostgreSQL mode) and a mocked Redis.
 * Each test class gets a fresh DB thanks to {@code @DirtiesContext}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Redis is not available in IT — stub it so beans that depend on it still load.
    @MockBean
    protected StringRedisTemplate redisTemplate;

    @SuppressWarnings("unchecked")
    protected void stubRedis() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
        lenient().when(ops.get(anyString())).thenReturn(null);
    }

    // --------------- auth helpers ---------------

    protected String registerAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterRequest(username, password));
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        AuthResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return resp.token();
    }

    protected String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequest(username, password));
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        AuthResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return resp.token();
    }

    protected String adminToken() throws Exception {
        // The BootstrapAdminRunner seeds admin/admin123! on startup.
        return loginAndGetToken("admin", "admin123!");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
