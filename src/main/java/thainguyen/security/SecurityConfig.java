package thainguyen.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import thainguyen.data.UserRepository;
import thainguyen.domain.User;

import java.util.Optional;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /*
    *
    - BCryptPasswordEncoder: Áp dụng mã hóa bảo mật bcrypt mạnh mẽ.
    - NoOpPasswordEncoder: Không áp dụng mã hóa.
    - Pbkdf2PasswordEncoder: Áp dụng mã hóa PBKDF2.
    - SCryptPasswordEncoder: Áp dụng mã hóa bảo mật Scrypt.
    - StandardPasswordEncoder: Áp dụng mã hóa bảo mật SHA-256.
    * */

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return (username) -> {
            Optional<User> userOpt = repo.findByUsername(username);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
            throw new UsernameNotFoundException("User '" + username + "' not found");
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // develop env
                .authorizeHttpRequests((authz) -> authz
                        /*.requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")*/
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "EMPLOYEE", "CUSTOMER")

                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/brands/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/brands/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/brands/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/categories/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/sizes/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/sizes/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/sizes/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/detailproducts/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/detailproducts/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/detailproducts/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers("/api/orders/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/discounts/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/discounts/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/discounts/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

}
