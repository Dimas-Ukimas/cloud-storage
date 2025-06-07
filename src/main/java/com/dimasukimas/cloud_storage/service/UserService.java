package com.dimasukimas.cloud_storage.service;

import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.mapper.UserMapper;
import com.dimasukimas.cloud_storage.model.Role;
import com.dimasukimas.cloud_storage.model.User;
import com.dimasukimas.cloud_storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsImpl signUp(AuthRequestDto userInfo) {

        String encodedPassword = passwordEncoder.encode(userInfo.password());

        User user = User.builder()
                .username(userInfo.username())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        return mapper.toSignInDto(userRepository.save(user));
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(mapper::toSignInDto)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " was not found"));
    }
}
