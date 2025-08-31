package com.dimasukimas.cloud_storage.config.event;

import com.dimasukimas.cloud_storage.repository.StorageRepository;
import com.dimasukimas.cloud_storage.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final StorageRepository repository;

@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegisteredEvent(UserRegisteredEvent event){

    repository.createDirectory(PathUtils.createUserDirectoryName(event.userId()));
}

}
