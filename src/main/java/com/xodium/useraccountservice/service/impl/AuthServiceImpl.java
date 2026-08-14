package com.xodium.useraccountservice.service.impl;

import com.xodium.useraccountservice.dto.*;
import com.xodium.useraccountservice.entity.Account;
import com.xodium.useraccountservice.entity.Role;
import com.xodium.useraccountservice.entity.User;
import com.xodium.useraccountservice.enums.AccountStatus;
import com.xodium.useraccountservice.enums.AccountType;
import com.xodium.useraccountservice.enums.Currency;
import com.xodium.useraccountservice.exceptions.BadRequestException;
import com.xodium.useraccountservice.exceptions.NotFoundException;
import com.xodium.useraccountservice.kafka.dto.UserRegistrationEvent;
import com.xodium.useraccountservice.kafka.service.AccountEventPublisher;
import com.xodium.useraccountservice.repository.AccountRepository;
import com.xodium.useraccountservice.repository.RoleRepository;
import com.xodium.useraccountservice.repository.UserRepository;
import com.xodium.useraccountservice.security.JwtService;
import com.xodium.useraccountservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    private final AccountEventPublisher accountEventPublisher;

    @Override
    public ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest) {
        log.info("Trying to register user: {}", registrationRequest);
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Account already exist for this email");
        }
        Set<Role> roles = new HashSet<>();
        String roleName = (registrationRequest.getRole() != null && !registrationRequest.getRole().isBlank())
                ? registrationRequest.getRole().toUpperCase() :
                "CUSTOMER";
        Role databaseRole = roleRepository.findByName(roleName).orElseThrow(() -> new NotFoundException(String.format("Role %s not found", roleName)));
        roles.add(databaseRole);
        User userToSave = User.builder()
                .roles(roles)
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .enabled(true)
                .build();
        User savedUser = userRepository.save(userToSave);

        String accountNumber = generateAccountNumber();
        Account accountToSaveToDb = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(Currency.NGN)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .user(savedUser)
                .build();
        accountRepository.save(accountToSaveToDb);


        UserRegistrationEvent userRegistrationEvent = UserRegistrationEvent.builder()
                .accountNumber(accountNumber)
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .build();
        accountEventPublisher.publishUserRegistrationEvent(userRegistrationEvent);

        String token = jwtService.generateToken(savedUser.getEmail());
        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();

        return new ApiResponse<>(HttpStatus.CREATED.value(), "User account created successfully", authResponse);
    }

    @Override
    public ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest) {
        log.info("Login with this request: {}", loginRequest);
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User/Password wrong"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("User/Password wrong");
        }
        if (!user.isEnabled()) throw new BadRequestException("Disabled user");

        String token = jwtService.generateToken(user.getEmail());
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
        return new ApiResponse<>(HttpStatus.OK.value(), "Login successful", authResponse);
    }

    private String generateAccountNumber() {
        String accountNumber;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            int randomPart = random.nextInt(100_000_000);
            accountNumber = String.format("00%08d", randomPart);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
