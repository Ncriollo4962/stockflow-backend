package com.stockflow.core.config;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ApplicationConfig {

    private final UserDetailsService userDetailsService;

    public ApplicationConfig(@Qualifier("dbUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Le decimos quién busca los usuarios en la DB
        authProvider.setUserDetailsService(userDetailsService);
        // Le decimos qué algoritmo usar para comparar contraseñas
        authProvider.setPasswordEncoder(passwordEncoder());

        authProvider.setHideUserNotFoundExceptions(false);
        
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // El estándar de cifrado actual
    }

    @Bean
    public DeadlockRetryExecutor deadlockRetryExecutor(@NonNull PlatformTransactionManager transactionManager) {
        return new DeadlockRetryExecutor(transactionManager);
    }

    public static final class DeadlockRetryExecutor {
        private final @NonNull PlatformTransactionManager transactionManager;

        public DeadlockRetryExecutor(@NonNull PlatformTransactionManager transactionManager) {
            this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        }

        public <T> T execute(Supplier<T> action) {
            int maxAttempts = 3;
            long baseBackoffMs = 150;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    return executeInNewTransaction(action);
                } catch (PessimisticLockingFailureException ex) {
                    if (attempt == maxAttempts) {
                        throw ex;
                    }
                    try {
                        Thread.sleep(baseBackoffMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                }
            }

            throw new IllegalStateException("No se pudo completar la operación.");
        }

        public void run(Runnable action) {
            execute(() -> {
                action.run();
                return null;
            });
        }

        private <T> T executeInNewTransaction(Supplier<T> action) {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return template.execute(status -> action.get());
        }
    }
}
