package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BankCardException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BankCardException("Email already exists: " + dto.getEmail());
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole() != null ? dto.getRole() : User.Role.USER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Created user: {}", saved.getUsername());

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankCardException("User not found: " + id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(int page, int size, String sortBy) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy)))
                .map(this::mapToDto);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankCardException("User not found: " + id));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new BankCardException("Email already exists: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getEnabled() != null) user.setEnabled(dto.getEnabled());

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BankCardException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user: {}", id);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}


