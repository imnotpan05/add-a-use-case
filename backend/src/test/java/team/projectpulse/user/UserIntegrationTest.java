package team.projectpulse.user;

import team.projectpulse.AbstractIntegrationTest;
import org.json.JSONObject;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration tests for User API endpoints")
class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    UserRepository userRepository;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Test
    void testCheckUserExists1() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/exists/j.smith@abc.edu").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value(""))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testCheckUserExists2() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/exists/m.long@abc.edu").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value(""))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void testPreviouslyIssuedTokenIsRejectedAfterAccountIsDisabled() throws Exception {
        var user = this.userRepository.findByEmail("j.smith@abc.edu").orElseThrow();
        String loginResponse = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                        .with(httpBasic("j.smith@abc.edu", "123456")))
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + new JSONObject(loginResponse).getJSONObject("data").getString("token");

        String studentPath = this.baseUrl + "/students/" + user.getId();
        this.mockMvc.perform(get(studentPath).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());

        user.setEnabled(false);
        this.userRepository.save(user);

        this.mockMvc.perform(get(studentPath).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

}
