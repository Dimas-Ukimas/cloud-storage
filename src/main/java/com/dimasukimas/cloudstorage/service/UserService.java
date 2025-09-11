package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.event.UserRegisteredEvent;
import com.dimasukimas.cloudstorage.dto.AuthRequestDto;
import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.exception.UsernameAlreadyExistsException;
import com.dimasukimas.cloudstorage.mapper.UserMapper;
import com.dimasukimas.cloudstorage.model.Role;
import com.dimasukimas.cloudstorage.model.User;
import com.dimasukimas.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CustomUserDetails signUp(AuthRequestDto userInfo) {
        String encodedPassword = passwordEncoder.encode(userInfo.password());

        User user = User.builder()
                .username(userInfo.username())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        User savedUser;

        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));

        return mapper.toUserDetails(savedUser);
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(mapper::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));
    }
}
