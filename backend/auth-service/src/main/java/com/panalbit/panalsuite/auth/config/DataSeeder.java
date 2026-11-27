package com.panalbit.panalsuite.auth.config;

import com.panalbit.panalsuite.auth.model.Role;
import com.panalbit.panalsuite.auth.model.UserAccount;
import com.panalbit.panalsuite.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Carga los usuarios de demostracion la primera vez que arranca el servicio. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        migrate("solicitante", "mizahel", "Mizahel", Role.SOLICITANTE);
        migrate("resolutor", "carlos", "Carlos", Role.RESOLUTOR);
        migrate("supervisor", "darliette", "Darliette", Role.SUPERVISOR);
        migrate("gerencia", "jose", "José", Role.GERENCIA);
        migrate("admin", "thomas", "Thomas", Role.ADMINISTRADOR);
    }

    private void migrate(String legacy, String username, String name, Role role) {
        String email = username + "@panalsui.com";
        var old = repository.findByUsernameIgnoreCase(legacy).filter(u -> u.getRole() == role);
        var current = repository.findByUsernameIgnoreCase(username);
        // Never merge or delete accounts: IDs are referenced by the tickets service.
        if (old.isPresent() && current.isPresent() && !old.get().getId().equals(current.get().getId())) {
            throw new IllegalStateException("Conflicto de cuentas de demostración: " + legacy + " / " + username);
        }
        UserAccount user = old.or(() -> current).orElseGet(UserAccount::new);
        if (user.getId() != null && user.getRole() != role) {
            throw new IllegalStateException("Rol incompatible para cuenta de demostración: " + username);
        }
        if (repository.findAllByEmailIgnoreCase(email).stream().anyMatch(u -> !u.getId().equals(user.getId()))) {
            throw new IllegalStateException("Correo de demostración duplicado: " + email);
        }
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        if (user.getPasswordHash() == null || !passwordEncoder.matches("1234", user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode("1234"));
        }
        repository.save(user);
    }
}
