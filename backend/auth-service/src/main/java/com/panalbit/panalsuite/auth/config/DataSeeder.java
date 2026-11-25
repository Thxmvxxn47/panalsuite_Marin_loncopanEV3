package com.panalbit.panalsuite.auth.config;

import com.panalbit.panalsuite.auth.model.Role;
import com.panalbit.panalsuite.auth.model.UserAccount;
import com.panalbit.panalsuite.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea las cuentas de demostración o actualiza las del Avance 3. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean empty = repository.count() == 0;
        updateDemo(empty, "solicitante", "ana.garcia", "mizahel", "Mizahel", Role.SOLICITANTE, "pass123");
        updateDemo(empty, "resolutor", "carlos.perez", "carlos", "Carlos", Role.RESOLUTOR, "pass123");
        updateDemo(empty, "supervisor", "maria.torres", "darliette", "Darliette", Role.SUPERVISOR, "pass123");
        updateDemo(empty, "gerencia", "roberto.vega", "jose", "José", Role.GERENCIA, "pass123");
        updateDemo(empty, "admin", "luis.morales", "thomas", "Thomas", Role.ADMINISTRADOR, "admin123");
    }

    private void updateDemo(boolean empty, String oldUsername, String oldEmail, String username,
                            String name, Role role, String oldPassword) {
        String email = username + "@panalsui.com";
        if (empty) {
            repository.save(new UserAccount(username, name, email, role, passwordEncoder.encode("1234")));
            return;
        }
        var legacy = repository.findByUsername(oldUsername)
                .filter(user -> user.getRole() == role && user.getEmail().equalsIgnoreCase(oldEmail + "@empresa.cl"));
        if (legacy.isEmpty() || repository.existsByUsername(username)
                || !repository.findAllByEmailIgnoreCase(email).isEmpty()) {
            return;
        }
        UserAccount user = legacy.get();
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode("1234"));
        }
        repository.save(user);
    }
}
