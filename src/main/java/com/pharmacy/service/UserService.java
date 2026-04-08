package com.pharmacy.service;

import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;




import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(@NonNull Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public void deleteUser(@NonNull Long id) {
        User user = getUserById(id);
        userRepository.delete(java.util.Objects.requireNonNull(user));
    }

    public User updateRole(@NonNull Long id, @NonNull User.Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(java.util.Objects.requireNonNull(user));
    }

    public User updateStatus(@NonNull Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(java.util.Objects.requireNonNull(user));
    }


}
