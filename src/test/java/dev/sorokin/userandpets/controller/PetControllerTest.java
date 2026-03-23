package dev.sorokin.userandpets.controller;

import dev.sorokin.userandpets.converter.PetDtoConverter;
import dev.sorokin.userandpets.dto.PetDto;
import dev.sorokin.userandpets.model.Pet;
import dev.sorokin.userandpets.model.User;
import dev.sorokin.userandpets.service.PetService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetService petService;

    @Autowired
    private PetDtoConverter petDtoConverter;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() {
        userService.getUsers().clear();
    }


    @Test
    void shouldSuccessCreatePet() throws Exception {

        User user = userService.createUser(
                new User(null, "Dima", "Life@mail.ru", 22, List.of())
        );

        PetDto petDto = new PetDto(null, "pet1", user.id());

        String petDtoJson = objectMapper.writeValueAsString(petDto);

        String requestPetDtoJson = mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petDtoJson))
                        .andExpect(status().is(201))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PetDto responsePetDto = objectMapper.readValue(requestPetDtoJson, PetDto.class);


        Assertions.assertNotNull(responsePetDto.id());
        Assertions.assertEquals(petDto.name(), responsePetDto.name());
        Assertions.assertEquals(petDto.userId(), responsePetDto.userId());

        Assertions.assertDoesNotThrow(() -> petService.getPet(responsePetDto.id()));

        User userWithPet = userService.findUserById(user.id());
        Assertions.assertEquals(1, userWithPet.pets().size());
        Assertions.assertEquals(responsePetDto.id(), userWithPet.pets().get(0).id());
    }


    @Test
    void shouldNotCreatePetWhenRequestNotValid() throws Exception {
        User user = userService.createUser(
                new User(null, "Dima", "Life@mail.ru", 22, List.of())
        );

        PetDto petDto = new PetDto(null, null, user.id());

        String petDtoJson = objectMapper.writeValueAsString(petDto);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petDtoJson))
                        .andExpect(status().is(400));
    }


    @Test
    void shouldSuccessUpdatePet() throws Exception {

        User user = userService.createUser(
                new User(null, "Dima", "Life@mail.ru", 22, List.of())
        );

        PetDto petDto = new PetDto(null, "pet1", user.id());
        Pet petToCreate = petService.createPet(petDtoConverter.toPet(petDto));

        PetDto petToUpdate = new PetDto(petToCreate.id(), "updateName", user.id());

        String petToUpdateJson = objectMapper.writeValueAsString(petToUpdate);

        String requestPetToUpdateJson = mockMvc.perform(put("/api/pets/{id}", petToCreate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petToUpdateJson))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PetDto responsePet = objectMapper.readValue(requestPetToUpdateJson, PetDto.class);

        Assertions.assertNotNull(responsePet.id());
        Assertions.assertEquals("updateName", responsePet.name());
        Assertions.assertEquals(petToCreate.userId(), responsePet.userId());
        Assertions.assertEquals(petToCreate.id(), responsePet.id());

        User userWithPet = userService.findUserById(user.id());
        Assertions.assertEquals("updateName", userWithPet.pets().get(0).name());
        Assertions.assertEquals(responsePet.id(), userWithPet.pets().get(0).id());
    }


    @Test
    void shouldSuccessDeletePet() throws Exception {
        User user = userService.createUser(
                new User(null, "Dima", "Life@mail.ru", 22, List.of())
        );

        PetDto petDto = new PetDto(null, "pet1", user.id());
        Pet petToCreate = petService.createPet(petDtoConverter.toPet(petDto));

        mockMvc.perform(delete("/api/pets/{id}", petToCreate.id()))
                    .andExpect(status().is(204));
    }


    @Test
    void shouldNoSuccessDeletePet() throws Exception {
        mockMvc.perform(delete("/api/pets/{id}", Integer.MAX_VALUE))
                .andExpect(status().is(404));
    }


    @Test
    void shouldSuccessFindPetById() throws Exception {
        User user = userService.createUser(
                new User(null, "Dima", "Life@mail.ru", 22, List.of())
        );

        PetDto petDto = new PetDto(null, "pet1", user.id());
        Pet petToCreate = petService.createPet(petDtoConverter.toPet(petDto));

        String petJson = objectMapper.writeValueAsString(petToCreate);

        String requestFindPetById = mockMvc.perform(get("/api/pets/{id}", petToCreate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                        .andExpect(status().is(302))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PetDto responsePet = objectMapper.readValue(requestFindPetById, PetDto.class);

        org.assertj.core.api.Assertions.assertThat(petToCreate)
                .usingRecursiveComparison()
                .isEqualTo(responsePet);
    }


    @Test
    void shouldNoSuccessFindPetById() throws Exception {
        mockMvc.perform(get("/api/pets/{id}", Integer.MAX_VALUE))
                .andExpect(status().is(404));
    }

}
