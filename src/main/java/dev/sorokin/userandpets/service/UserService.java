package dev.sorokin.userandpets.service;

import dev.sorokin.userandpets.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {


    private final Map<Long, User> users;
    private final AtomicLong userIdSequence;


    public UserService() {
        this.users = new ConcurrentHashMap<>();
        this.userIdSequence = new AtomicLong(0);
    }

    public Map<Long, User> getUsers() {
        return users;
    }

    public User createUser(User user) {

        boolean existsUser = users.values().stream().anyMatch(u -> u.email().equals(user.email()));

        if (user.id() != null) {
            throw new IllegalArgumentException("Id for user should not be provided");
        }
        if (user.pets() != null && !user.pets().isEmpty()) {
            throw new IllegalArgumentException("User pets must be empty");
        }
        if (existsUser) {
            throw new IllegalArgumentException("User already exists.");
        }

        Long id = userIdSequence.incrementAndGet();

        User saveUser = new User(id, user.name(), user.email(), user.age(), new ArrayList<>());

        users.put(id, saveUser);
        return saveUser;
    }


    public User updateUser(User user) {

        if (user.id() == null) {
            throw new IllegalArgumentException("No user id passed");
        }

        if (!users.containsKey(user.id())) {
            throw new NoSuchElementException("No found user by id=%s".formatted(user.id()));
        }

        users.put(user.id(), user);
        return user;
    }


    public void deleteUser(Long id) {
        var result = users.remove(id);

        if (result == null) {
            throw new NoSuchElementException("No found user by id=%s".formatted(id));
        }
    }


    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NoSuchElementException("No such user with id=%s".formatted(id));
        }
        return users.get(id);
    }


    public List<User> searchUsers(String name, Integer age) {
        return users.values().stream()
                .filter(user -> name == null || user.name().equals(name))
                .filter(user -> age == null || user.age().equals(age))
                .toList();
    }


    public List<User> getAllUsers() {
        return users.values().stream().toList();
    }

}
