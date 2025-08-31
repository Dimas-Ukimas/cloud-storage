package com.dimasukimas.cloud_storage.helper;

import com.dimasukimas.cloud_storage.model.Role;
import com.dimasukimas.cloud_storage.model.User;
import com.dimasukimas.cloud_storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserTestDataHelper {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void clearRepository() {
        userRepository.deleteAll();
    }

    public void createUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    public Optional<User> findUser(String name) {
        return userRepository.findByUsername(name);
    }

}
