package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;


@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "IDX_USERNAME", columnList = "username"),
        @Index(name = "IDX_USERNAME_EMAIL", columnList = "username, email")
})
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @NotBlank(message = "Username attribute must not be null and empty!")
    @Column(unique = true)
    private String username;
    @NotBlank(message = "Password attribute must not be null and empty!")
    private String password;
    @NotBlank(message = "Email attribute must not be null and empty!")
    @Column(unique = true)
    private String email;
    @NotBlank(message = "Fullname attribute must not be null and empty!")
    private String fullname;
    @NotBlank(message = "Gender attribute must not be null and empty!")
    private String gender;

    @NotNull(message = "Username attribute must not be null!")
    @Min(value = 18, message = "Minimum age is 18")
    private Integer age;

    private String avatar;

    @NotNull(message = "Position attribute must not be null!")
    @Enumerated(EnumType.STRING)
    private Position position;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Address> addresses = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime registerdAt;

    public static enum Position {
        ADMIN, EMPLOYEE, CUSTOMER
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + position));
    }

    public User(String username, String password
            , String email
            , String fullname
            , String gender
            , Integer age
            , String avatar
            , Position position) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.gender = gender;
        this.age = age;
        this.avatar = avatar;
        this.position = position;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

}
