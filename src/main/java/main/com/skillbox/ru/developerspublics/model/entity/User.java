package main.com.skillbox.ru.developerspublics.model.entity;

import lombok.*;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
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

    //get timestamp in seconds TODO
    public long getTimestamp() {
        return regTime.getTime() / 1000;
    }

    //timestamp in milliseconds to java.util.Date
    public void setRegTime(long timestamp) {
        regTime = Date.from(Instant.ofEpochMilli(timestamp));
    }


    //конструктор для регистрации
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.isModerator = 0;
        setRegTime(Instant.now().toEpochMilli());
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

    public long userHashCode() {
        long result = id + 37;
        result = result * (isModerator == 1 ? id : 0) + 37;
        DateFormat dateFormat = new SimpleDateFormat("HHmmddMMyy");
        result = result * Long.parseLong(dateFormat.format(regTime));
        result = result * name.length();
        result = result * email.length();
        result = result * password.length();
        return result;
    }
}
