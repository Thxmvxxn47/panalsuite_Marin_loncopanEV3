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
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        save("solicitante", "Ana García López", "ana.garcia@empresa.cl", Role.SOLICITANTE, "pass123");
        save("resolutor", "Carlos Pérez Muñoz", "carlos.perez@empresa.cl", Role.RESOLUTOR, "pass123");
        save("supervisor", "María Torres Silva", "maria.torres@empresa.cl", Role.SUPERVISOR, "pass123");
        save("gerencia", "Roberto Vega Ramos", "roberto.vega@empresa.cl", Role.GERENCIA, "pass123");
        save("admin", "Luis Morales Castro", "luis.morales@empresa.cl", Role.ADMINISTRADOR, "admin123");
    }

    private void save(String username, String name, String email, Role role, String password) {
        repository.save(new UserAccount(username, name, email, role, passwordEncoder.encode(password)));
    }
}
