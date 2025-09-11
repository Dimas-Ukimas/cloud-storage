package com.dimasukimas.cloudstorage.util.assertion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class AuthAssert<T extends AuthAssert<T>> {

    private final RedisAssert redisAssert;
    private final UserAssert userAssert;
    private final HttpAssert httpAssert;

    protected T self() {
        return (T) this;
    }

    public static AuthAssert create(RedisAssert redisAssert, UserAssert userAssert, HttpAssert httpAssert){
        return new AuthAssert(redisAssert, userAssert, httpAssert);
    }

    public T assertStatus(HttpStatus status) {
        httpAssert.assertStatus(status);
        return self();
    }

    public T assertJsonContentType() {
        httpAssert.assertJsonContentType();
        return self();
    }

    public T assertBodyContainsUsername(String username) {
        httpAssert.assertBodyContainsUsername(username);
        return self();
    }

    public T assertSetCookieHeader() {
        httpAssert.assertSetCookieHeader();
        return self();
    }

    public T assertBodyContainsMessage(String message) {
        httpAssert.assertBodyContainsMessage(message);
        return self();
    }

    public T assertRedisSessionCreated() {
        redisAssert.assertRedisSessionCreated();
        return self();
    }

    public T assertUserExists(String username) {
        userAssert.assertUserExists(username);
        return self();
    }

    public T assertUserNotExists(String username) {
        userAssert.assertUserNotExists(username);
        return self();
    }

    public T assertRedisSessionNotCreated() {
        redisAssert.assertRedisSessionNotCreated();
        return self();
    }

}
