package com.dimasukimas.cloud_storage.util.assertion;

import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.exception.handler.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpAssert {

    ResponseEntity<?> response;

    private HttpAssert(ResponseEntity<?> response) {
        this.response = response;
    }

    public static HttpAssert create(ResponseEntity<?> response) {
        return new HttpAssert(response);
    }

    public HttpAssert assertJsonContentType() {
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        return this;
    }

    public HttpAssert assertStatus(HttpStatus status) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        return this;
    }

    public HttpAssert assertBodyContainsUsername(String username) {
        assertThat(Objects.requireNonNull(castToUsernameDto(response).getBody()).username().contains(username));
        return this;
    }

    public HttpAssert assertSetCookieHeader() {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("SESSION"));
        return this;
    }

    public HttpAssert assertBodyContainsMessage(String message) {
        assertThat(Objects.requireNonNull(castToErrorResponse(response).getBody()).message()).contains(message);
        return this;
    }

    private ResponseEntity<UsernameDto> castToUsernameDto(ResponseEntity<?> response) {
        return (ResponseEntity<UsernameDto>) response;
    }

    private ResponseEntity<ErrorResponse> castToErrorResponse(ResponseEntity<?> response) {
        return (ResponseEntity<ErrorResponse>) response;
    }

}
