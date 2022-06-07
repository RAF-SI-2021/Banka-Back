package rs.edu.raf.banka.user_service.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Admin123\"}"))
                        .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        token = mvcResult.getResponse().getContentAsString().replace("\"", "");
    }

    @Test
    void createAndVerifyUser() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        cuf.setIme("Test");
        cuf.setPrezime("Testic");
        cuf.setEmail("test@raf.rs");
        cuf.setJmbg("1234567980123");
        cuf.setBrTelefon("0690000000");
        cuf.setPozicija("ROLE_ADMIN");
        cuf.setLimit(0.0);
        cuf.setNeedsSupervisorPermission(false);

        mockMvc.perform(post("/api/user/create")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(cuf)))
                        .andExpect(status().isOk());

        Optional<User> user = userRepository.findByEmail("test@raf.rs");
        assertThat(user.isEmpty()).isEqualTo(false);
        assertThat(user.get().getJmbg()).isEqualTo("1234567980123");
    }

    @Test
    void getUserByToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/user")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(""))
                        .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        User user = objectMapper.readValue(strResp, User.class);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin");
    }

    @Test
    void getUsers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/users")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(""))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<User> users = objectMapper.readValue(strResp, new TypeReference<List<User>>() {});
        assertThat(users.size()).isNotEqualTo(0);
    }
}
