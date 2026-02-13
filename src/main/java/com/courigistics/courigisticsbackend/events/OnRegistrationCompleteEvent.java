package com.courigistics.courigisticsbackend.events;

import com.courigistics.courigisticsbackend.entities.Account;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private final Account account;
    private final String token;

    public OnRegistrationCompleteEvent(Account account, String token) {
        super(account);
        this.account = account;
        this.token = token;
    }
}
