package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "IDX_USERNAME", columnList = "username"),
        @Index(name = "IDX_USERNAME_EMAIL", columnList = "username, email")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    private @NotNull String username;
    private @NotNull String password;
    private @NotNull String email;
    private @NotNull String fullname;
    private @NotNull String gender;
    private @NotNull Integer age;
    private String avatar;
    private @NotNull String position;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Address> addresses = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime registerdAt;

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
            , String position) {
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
