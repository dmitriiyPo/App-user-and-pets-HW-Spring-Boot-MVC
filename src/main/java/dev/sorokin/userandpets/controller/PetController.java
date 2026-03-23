package dev.sorokin.userandpets.controller;

import dev.sorokin.userandpets.converter.PetDtoConverter;
import dev.sorokin.userandpets.dto.PetDto;
import dev.sorokin.userandpets.model.Pet;
import dev.sorokin.userandpets.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final PetDtoConverter petDtoConverter;

    @Autowired
    public PetController(PetService petService, PetDtoConverter petDtoConverter) {
        this.petService = petService;
        this.petDtoConverter = petDtoConverter;
    }


    @PostMapping
    public ResponseEntity<PetDto> createPet(@RequestBody @Valid PetDto petDto) {
        Pet newPet = petService.createPet(petDtoConverter.toPet(petDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(petDtoConverter.toDto(newPet));
    }


    @PutMapping("/{id}")
    public ResponseEntity<PetDto> updatePet(@PathVariable("id") Long id, @RequestBody @Valid PetDto petDto) {
        Pet petToUpdate = new Pet(id, petDto.name(), petDto.userId());
        Pet updatePet = petService.updatePet(petToUpdate);
        return ResponseEntity.ok(petDtoConverter.toDto(updatePet));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable("id") Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<PetDto> findPetById(@PathVariable Long id) {
        Pet pet = petService.getPet(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(petDtoConverter.toDto(pet));
    }

}
