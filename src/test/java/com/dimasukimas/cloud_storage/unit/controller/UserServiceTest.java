package com.dimasukimas.cloud_storage.unit.controller;

import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.dto.AuthResponseDto;
import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.mapper.UserMapper;
import com.dimasukimas.cloud_storage.model.Role;
import com.dimasukimas.cloud_storage.model.User;
import com.dimasukimas.cloud_storage.repository.UserRepository;
import com.dimasukimas.cloud_storage.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldEncodePasswordWhenSignUpUser() {

        AuthRequestDto userInfo = new AuthRequestDto("user", "rawPassword");
        String encodedPassword = "encodedPassword";

        User userToSave = User.builder()
                .username("user")
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("rawPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(userToSave);
        when(userMapper.toSignUpDto(any(User.class))).thenReturn(new AuthResponseDto("user"));

        UserDetailsImpl response = userService.signUp(userInfo);

        verify(passwordEncoder).encode("rawPassword");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(encodedPassword).isEqualTo(captor.getValue().getPassword());
        assertThat(userToSave.getUsername()).isEqualTo(response.username());
    }

}
