package dev.sorokin.userandpets.converter;

import dev.sorokin.userandpets.dto.PetDto;
import dev.sorokin.userandpets.model.Pet;
import org.springframework.stereotype.Component;

@Component
public class PetDtoConverter {

    public PetDto toDto(Pet pet) {
        return new PetDto(
                pet.id(),
                pet.name(),
                pet.userId()
        );
    }

    public Pet toPet(PetDto petDto) {
        return new Pet(
                petDto.id(),
                petDto.name(),
                petDto.userId()
        );
    }

}
