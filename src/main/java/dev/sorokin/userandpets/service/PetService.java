package dev.sorokin.userandpets.service;

import dev.sorokin.userandpets.model.Pet;
import dev.sorokin.userandpets.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PetService {

    private final UserService userService;
    private final AtomicLong petIdSequence;

    @Autowired
    public PetService(UserService userService) {
        this.userService = userService;
        this.petIdSequence = new AtomicLong(0);
    }


    public Pet createPet(Pet pet) {
        if (pet.id() != null) {
            throw new IllegalArgumentException("Id for pet should not be provided");
        }

        Pet petToSave = new Pet(petIdSequence.incrementAndGet(), pet.name(), pet.userId());
        userService.findUserById(pet.userId()).pets().add(petToSave);
        return petToSave;
    }


    public Pet updatePet(Pet pet) {
        if (pet.id() == null) {
            throw new NoSuchElementException("No found pet by id=%s".formatted(pet.userId()));
        }

        var foundPet = findPetById(pet.id())
                .orElseThrow(() -> new NoSuchElementException("No such pet with id=%s".formatted(pet.id())));

        var updatePet = new Pet(foundPet.id(), pet.name(), foundPet.userId());

        User user = userService.findUserById(pet.userId());
        user.pets().remove(foundPet);
        user.pets().add(updatePet);
        return updatePet;
    }


    public void deletePet(Long id) {

        Pet foundPet = findPetById(id)
                .orElseThrow(() -> new NoSuchElementException("No such pet with id=%s".formatted(id)));

        User user = userService.findUserById(foundPet.userId());
        user.pets().remove(foundPet);
    }


    public Pet getPet(Long id) {
        return findPetById(id)
                .orElseThrow(() -> new NoSuchElementException("No found pet by id=%s".formatted(id)));
    }


    private Optional<Pet> findPetById(Long id) {
        return userService.getAllUsers().stream()
                .flatMap(user -> user.pets().stream())
                .filter(pet -> pet.id().equals(id))
                .findAny();
    }

}
