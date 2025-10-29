package com.dimasukimas.cloudstorage.util.assertion;

import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAssert {

    private final UserTestDataHelper userHelper;

    private UserAssert(UserTestDataHelper userHelper) {
        this.userHelper = userHelper;
    }

    public static UserAssert create(UserTestDataHelper userHelper){
        return new UserAssert(userHelper);
    }

    public UserAssert assertUserExists(String username) {
        assertThat(userHelper.findUser(username)).isPresent();
        return this;
    }

    public UserAssert assertUserNotExists(String username) {
        assertThat(userHelper.findUser(username)).isEmpty();
        return this;
    }

}
