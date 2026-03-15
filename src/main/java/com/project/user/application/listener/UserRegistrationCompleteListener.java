package com.project.user.application.listener;

import com.project.user.application.dto.request.UserRegistrationCompletedEvent;
import com.project.user.domain.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegistrationCompleteListener {

    private final EmailAuthRepository emailAuthRepository;

    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCompleted(UserRegistrationCompletedEvent event) {
        emailAuthRepository.deleteRegisterSession(event.email());
    }

}
