package com.server.app.config;

import com.server.app.security.JwtAuthenticationFilter;
import com.server.app.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private AuthenticationConfiguration authConfig;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void whenGetPasswordEncoder_thenReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isNotNull();
        
        String password = "testPassword";
        String encodedPassword = encoder.encode(password);
        
        assertThat(encodedPassword).isNotEqualTo(password);
        assertThat(encoder.matches(password, encodedPassword)).isTrue();
    }

    @Test
    void whenGetAuthenticationProvider_thenReturnConfiguredProvider() {
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        
        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    void whenGetAuthenticationManager_thenReturnManager() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager manager = securityConfig.authenticationManager(authConfig);
        
        assertThat(manager).isNotNull();
        assertThat(manager).isEqualTo(mockManager);
    }
}