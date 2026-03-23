package dev.sorokin.userandpets.controller;

import dev.sorokin.userandpets.converter.UserDtoConverter;
import dev.sorokin.userandpets.dto.UserDto;
import dev.sorokin.userandpets.model.User;
import dev.sorokin.userandpets.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDtoConverter userDtoConverter;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() {
        userService.getUsers().clear();
    }


    @Test
    void shouldSuccessCreateUser() throws Exception {

        UserDto userToCreate = new UserDto(null, "Dima", "Life@mail.ru", 22, List.of());
        String userToCreateJson = objectMapper.writeValueAsString(userToCreate);

        String createUserJson = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreateJson))
                .andExpect(status().is(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto userDtoResponse = objectMapper.readValue(createUserJson, UserDto.class);

        Assertions.assertNotNull(userDtoResponse.id());

        Assertions.assertEquals(userToCreate.name(), userDtoResponse.name());
        Assertions.assertEquals(userToCreate.age(), userDtoResponse.age());
        Assertions.assertEquals(userToCreate.email(), userDtoResponse.email());
        Assertions.assertEquals(userToCreate.pets(), userDtoResponse.pets());
    }


    @Test
    void shouldNotCreateUserWhenRequestNotValid() throws Exception {
        UserDto userToCreate = new UserDto(null, null, "Life@mail.ru", 22, List.of());
        String userToCreateJson = objectMapper.writeValueAsString(userToCreate);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userToCreateJson))
                        .andExpect(status().is(400));
    }


    @Test
    void shouldSuccessUpdateUser() throws Exception {

        UserDto userToCreate = new UserDto(null, "Dima", "Life@mail.ru", 22, List.of());
        User userCreate = userService.createUser(userDtoConverter.toUser(userToCreate));

        UserDto userToUpdate  = new UserDto(userCreate.id(), "updateValue", "update@mail.ru", 23, List.of());

        String userToUpdateJson = objectMapper.writeValueAsString(userToUpdate);

        String requestUserToUpdateJson = mockMvc.perform(put("/api/users/{id}", userCreate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userToUpdateJson))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        UserDto responseUserToUpdateJson = objectMapper.readValue(requestUserToUpdateJson, UserDto.class);

        Assertions.assertNotNull(responseUserToUpdateJson.id());

        Assertions.assertEquals(userCreate.id(), responseUserToUpdateJson.id());
        Assertions.assertEquals("updateValue", responseUserToUpdateJson.name());
        Assertions.assertEquals("update@mail.ru", responseUserToUpdateJson.email());
        Assertions.assertEquals(23, responseUserToUpdateJson.age());
    }


    @Test
    void shouldSuccessDeleteUser() throws Exception {

        UserDto userToCreate = new UserDto(null, "Dima", "Life@mail.ru", 22, List.of());
        User userCreate = userService.createUser(userDtoConverter.toUser(userToCreate));

        mockMvc.perform(delete("/api/users/{id}", userCreate.id()))
                .andExpect(status().is(204));
    }


    @Test
    void shouldNoSuccessDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", Integer.MAX_VALUE))
                .andExpect(status().is(404));
    }


    @Test
    void shouldSuccessFindUserById() throws Exception {
        UserDto userToCreate = new UserDto(null, "Dima", "Life@mail.ru", 22, List.of());
        User userCreate = userService.createUser(userDtoConverter.toUser(userToCreate));

        String requestUserById = mockMvc.perform(get("/api/users/{id}", userCreate.id()))
                    .andExpect(status().is(302))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

        UserDto responseFoundUser = objectMapper.readValue(requestUserById, UserDto.class);

        org.assertj.core.api.Assertions.assertThat(userCreate)
                .usingRecursiveComparison()
                .isEqualTo(responseFoundUser);
    }


    @Test
    void shouldNoSuccessFindUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", Integer.MAX_VALUE))
                .andExpect(status().is(404));
    }


    @Test
    void shouldSuccessSearchUserWithParams() throws Exception {
        UserDto userDto1 = new UserDto(null, "Dima", "Life272@mail.ru", 20, List.of());
        UserDto userDto2 = new UserDto(null, "Tima", "Tima@mail.ru", 20, List.of());
        UserDto userDto3 = new UserDto(null, "Kate", "Kate@mail.ru", 20, List.of());
        UserDto userDto4 = new UserDto(null, "Dima", "dima@mail.ru", 22, List.of());

        userService.createUser(userDtoConverter.toUser(userDto1));
        userService.createUser(userDtoConverter.toUser(userDto2));
        userService.createUser(userDtoConverter.toUser(userDto3));
        userService.createUser(userDtoConverter.toUser(userDto4));

        mockMvc.perform(get("/api/users")
                        .param("name", "Dima")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().is(302))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", everyItem(is("Dima"))))
                .andExpect(jsonPath("$[0].email", anyOf(is("Life272@mail.ru"), is("dima@mail.ru"))))
                .andExpect(jsonPath("$[1].email", anyOf(is("Life272@mail.ru"), is("dima@mail.ru"))));
    }

}
