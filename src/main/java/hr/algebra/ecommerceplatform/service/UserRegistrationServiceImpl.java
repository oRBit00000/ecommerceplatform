package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.RegistrationDTO;
import hr.algebra.ecommerceplatform.model.Role;
import hr.algebra.ecommerceplatform.model.User;
import hr.algebra.ecommerceplatform.repository.RoleRepository;
import hr.algebra.ecommerceplatform.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationServiceImpl(UserRepository userRepository,
                                       RoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerCustomer(RegistrationDTO registrationDTO) {
        if (userRepository.findByName(registrationDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role missing."));

        User user = new User();
        user.setName(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRoles(List.of(userRole));

        userRepository.save(user);
    }
}
