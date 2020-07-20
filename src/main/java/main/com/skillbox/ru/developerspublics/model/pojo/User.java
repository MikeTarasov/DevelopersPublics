package main.com.skillbox.ru.developerspublics.model.pojo;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.Role;
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


    //конструктор для регистрации
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        regTime = new Date(System.currentTimeMillis());
        setRoles();
    }

    public void setRoles() {
        roles.add(new Role(Roles.USER));
        if (isModerator == 1) roles.add(new Role(Roles.MODERATOR));
    }

    public void setRoles(Set<Role> set) {
        setRoles();
    }

    public Set<Role> getRoles() {
        if (roles.size() == 0) {
            setRoles();
        }
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return email;
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

    //    public String toString() {
//        //TODO убрать!!!
//        return "{\"id\": " + id + ", \"name\": " + name + "}";
//    }
//
//    public String toStringIdNamePhoto() {
//        //TODO убрать!!!
//        return "{\"id\": " + id + ", \"name\": " + name + ", \"photo\": " + photo + "}";
//    }
}
