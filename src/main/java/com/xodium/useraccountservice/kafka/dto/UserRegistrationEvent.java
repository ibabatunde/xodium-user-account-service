package com.xodium.useraccountservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationEvent {
    private String email;
    private String firstName;
    private String lastName;
    private String accountNumber;
    private String bankName = "XODIUM BANK";
}
