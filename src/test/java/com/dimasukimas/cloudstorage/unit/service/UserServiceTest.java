package com.dimasukimas.cloudstorage.unit.service;

import com.dimasukimas.cloudstorage.dto.AuthRequestDto;
import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.exception.UsernameAlreadyExistsException;
import com.dimasukimas.cloudstorage.mapper.UserMapper;
import com.dimasukimas.cloudstorage.model.Role;
import com.dimasukimas.cloudstorage.model.User;
import com.dimasukimas.cloudstorage.repository.UserRepository;
import com.dimasukimas.cloudstorage.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void signUp_shouldEncodePassword() {
        AuthRequestDto userInfo = new AuthRequestDto("user", "rawPassword");
        String encodedPassword = "encodedPassword";

        User userToSave = User.builder()
                .username("user")
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("rawPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(userToSave);
        when(userMapper.toUserDetails(any(User.class))).thenReturn(new CustomUserDetails(1L,"user", "encodedPassword", List.of()));

        CustomUserDetails response = userService.signUp(userInfo);

        verify(passwordEncoder).encode("rawPassword");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(encodedPassword).isEqualTo(captor.getValue().getPassword());
        assertThat(userToSave.getUsername()).isEqualTo(response.username());
    }

    @Test
    void signUp_whenUsernameExists_shouldThrowCustomException(){
        AuthRequestDto userInfo = new AuthRequestDto("user", "rawPassword");

        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate username"));

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.signUp(userInfo));
    }

}
