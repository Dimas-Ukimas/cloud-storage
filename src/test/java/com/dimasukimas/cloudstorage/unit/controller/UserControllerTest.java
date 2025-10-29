package com.dimasukimas.cloudstorage.unit.controller;


import com.dimasukimas.cloudstorage.controller.UserController;
import com.dimasukimas.cloudstorage.dto.UsernameRequestDto;
import com.dimasukimas.cloudstorage.exception.handler.GlobalExceptionHandler;
import com.dimasukimas.cloudstorage.mapper.UserMapper;
import com.dimasukimas.cloudstorage.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserMapper userMapper;

    @Captor
    ArgumentCaptor<UserDetails> argumentCaptor;

    @Test
    @WithMockUser
    void getCurrentUser_shouldReturnUsernameWithOk() throws Exception {
        UsernameRequestDto usernameRequestDto = new UsernameRequestDto("user");

        when(userMapper.toUserDto(any(CustomUserDetails.class)))
                .thenReturn(usernameRequestDto);

        mockMvc.perform(get("/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));

        verify(userMapper).toUserDto((CustomUserDetails) argumentCaptor.capture());
        UserDetails capturedUser = argumentCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("user");
    }
}
