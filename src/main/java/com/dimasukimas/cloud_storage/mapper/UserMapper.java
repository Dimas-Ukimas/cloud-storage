package com.dimasukimas.cloud_storage.mapper;

import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.dto.UserDto;
import com.dimasukimas.cloud_storage.model.Role;
import com.dimasukimas.cloud_storage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "authorities", expression = "java(getAuthorities(user.getRole()))")
    UserDetailsImpl toDto(User user);

    default Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    UserDto toUserDto(UserDetails user);
}
