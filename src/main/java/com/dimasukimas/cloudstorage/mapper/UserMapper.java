package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.dto.UsernameDto;
import com.dimasukimas.cloudstorage.model.Role;
import com.dimasukimas.cloudstorage.model.User;
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
    CustomUserDetails toUserDetails(User user);

    default Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    UsernameDto toUserDto(UserDetails user);
}
