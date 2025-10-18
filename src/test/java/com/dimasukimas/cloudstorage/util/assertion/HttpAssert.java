package com.dimasukimas.cloudstorage.util.assertion;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.dto.UsernameDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpAssert<T> {

    ResponseEntity<T> response;

    private HttpAssert(ResponseEntity<T> response) {
        this.response = response;
    }

    public static <T> HttpAssert create(ResponseEntity<T> response) {
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
        Object body = getBody().orElseThrow(() -> new NullPointerException("Body is null"));

        assertThat(body).isInstanceOf(UsernameDto.class);
        assertThat(((UsernameDto) body).username()).contains(username);
        return this;
    }

    //TODO переписать метод
    public HttpAssert assertSetCookieHeader() {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("SESSION"));
        return this;
    }

    public HttpAssert assertHeaderExist(String header) {
        assertThat(response.getHeaders()).containsKey(header);
        return this;
    }

    public HttpAssert assertHeaderNotExist(String header) {
        assertThat(response.getHeaders()).doesNotContainKey(header);
        return this;
    }


    public HttpAssert assertBodyContainsMessage(String message) {
        Object body = getBody().orElseThrow(() -> new NullPointerException("Body is null"));

        assertThat(body).isInstanceOf(ErrorResponse.class);
        assertThat(((ErrorResponse) body).message()).contains(message);
        return this;
    }

    public HttpAssert assertBodyContainsResourceName(String expectedName) {
        assertThat(bodyAsStream(ResourceInfoDto.class)
                .map(ResourceInfoDto::name)
                .filter(resource -> resource.equals(expectedName))
                .findAny())
                .isPresent();
        return this;
    }

    public HttpAssert assertResourceType(ResourceType type) {
        assertThat(bodyAsStream(ResourceInfoDto.class)
                .map(ResourceInfoDto::type)
                .equals(type.toString()));

        return this;
    }

    public HttpAssert assertEmptyCollectionBody() {
        Object body = getBody().orElseThrow(() -> new NullPointerException("Body is null"));
        if (body instanceof Collection<?> c) {
            assertThat(c).isEmpty();
            return this;
        }
        throw new AssertionError("Body is not a collection; cannot assert empty");
    }

    public HttpAssert assertBodyIsNull() {
        Optional<Object> body = getBody();
        assertThat(body).isEmpty();

        return this;
    }

    private Optional<Object> getBody() {

        return Optional.ofNullable(response.getBody());
    }

    private <E> Stream<E> bodyAsStream(Class<E> elementType) {
        Object body = getBody().orElseThrow(() -> new NullPointerException("Body is null"));

        if (elementType.isInstance(body)) {
            return Stream.of(elementType.cast(body));
        }
        if (body instanceof Collection<?> c) {
            return c.stream().filter(elementType::isInstance).map(elementType::cast);
        }
        throw new IllegalStateException("Unsupported body type: " + body.getClass());
    }

}
