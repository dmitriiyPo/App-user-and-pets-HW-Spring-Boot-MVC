package dev.sorokin.userandpets.converter;

import dev.sorokin.userandpets.dto.UserDto;
import dev.sorokin.userandpets.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter {

    private final PetDtoConverter petDtoConverter;

    @Autowired
    public UserDtoConverter(PetDtoConverter petDtoConverter) {
        this.petDtoConverter = petDtoConverter;
    }

    public User toUser(UserDto userDto) {
        return new User(
                userDto.id(),
                userDto.name(),
                userDto.email(),
                userDto.age(),
                userDto.pets().stream() .map(petDtoConverter::toPet).toList()
        );
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.id(),
                user.name(),
                user.email(),
                user.age(),
                user.pets().stream().map(petDtoConverter::toDto).toList()
        );
    }

}
