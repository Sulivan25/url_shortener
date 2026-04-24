package org.example.urlshortener.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminUserIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        stubRedis();
    }

    @Test
    void listUsers_admin_returns200() throws Exception {
        String adminTkn = adminToken();

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void listUsers_regularUser_returns403() throws Exception {
        String userToken = registerAndGetToken("blocked1", "password123");

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_admin_returns201() throws Exception {
        String adminTkn = adminToken();

        mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"newuser","password":"password123","role":"USER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void changeRole_admin_returns200() throws Exception {
        String adminTkn = adminToken();

        // Create a user first, then change their role
        String response = mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"rolechange","password":"password123","role":"USER"}
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/admin/users/" + userId + "/role")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deleteUser_admin_returns204() throws Exception {
        String adminTkn = adminToken();

        String response = mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"todelete","password":"password123","role":"USER"}
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/admin/users/" + userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn)))
                .andExpect(status().isNoContent());
    }

    @Test
    void anyAdminEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
