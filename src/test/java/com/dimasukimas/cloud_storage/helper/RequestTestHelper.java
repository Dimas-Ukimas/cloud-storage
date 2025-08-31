package com.dimasukimas.cloud_storage.helper;

import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestTestHelper {

    private final ObjectMapper objectMapper;

    public HttpEntity<String> authRequest(String username, String password){
        HttpHeaders headers = createJsonHeader();

        try {
            return new HttpEntity<>(objectMapper.writeValueAsString(new AuthRequestDto(username, password)), headers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build JSON request", e);
        }

    }

    private HttpHeaders createJsonHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

}
