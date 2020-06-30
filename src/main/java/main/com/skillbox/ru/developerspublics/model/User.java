package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import javax.persistence.*;
import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    //является ли модератором (isModerator = 1)
    @Column(name = "is_moderator", columnDefinition = "TINYINT", nullable = false)
    private int isModerator;

    //дата регистрации
    @Column(name = "reg_time", columnDefinition = "DATETIME", nullable = false)
    private Date regTime;

    //имя пользователя
    @Column(nullable = false)
    private String name;

    //почта пользователя
    @Column(nullable = false)
    private String email;

    //пароль пользователя
    @Column(nullable = false)
    private String password;

    //код для восстановления пароля
    private String code;

    //аватар
    @Column(columnDefinition = "TEXT")
    private String photo;

    //посты, требующие модерации
    @Transient
    @OneToMany(mappedBy = "moderatorPost", fetch = FetchType.LAZY)
    private List<Post> moderatorPosts;

    //посты пользователя
    @Transient
    @OneToMany(mappedBy = "userPost", fetch = FetchType.LAZY)
    private List<Post> userPosts;

    //оценки, данные пользователем
    @Transient
    @OneToMany(mappedBy = "userVote", fetch = FetchType.LAZY)
    private List<PostVote> userPostVotes;

    //комментарии пользователя
    @Transient
    @OneToMany(mappedBy = "commentUser", fetch = FetchType.LAZY)
    private List<PostComment> userPostComments;

    //список ролей пользователя
    @Transient
    private Set<Role> roles = new HashSet<>();


    //спец конструктор для гостевого входа
    public User(String name) {
        if (name.equals("GUEST")) {
            System.out.println("guest");
            this.name = name;
            password = name;
            email = name;
            roles.add(new Role(Roles.GUEST));
            regTime = new Date(System.currentTimeMillis());
        }
    }

    //конструктор для регистрации
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        regTime = new Date(System.currentTimeMillis());
    }

    public String toString() {
        return "{\"id\": " + id + ", \"name\": " + name + "}";
    }

    public String toStringIdNamePhoto() {
        return "{\"id\": " + id + ", \"name\": " + name + ", \"photo\": " + photo + "}";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
