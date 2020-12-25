package main.com.skillbox.ru.developerspublics.model.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.com.skillbox.ru.developerspublics.model.Role;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  //является ли модератором (isModerator = 1)
  @Column(columnDefinition = "INT", nullable = false)
  private int isModerator;

  //дата регистрации
  @Column(columnDefinition = "TIMESTAMP", nullable = false)
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

  //посты пользователя
  @OneToMany(mappedBy = "userPost", orphanRemoval = true)
  @LazyCollection(LazyCollectionOption.EXTRA)
  private List<Post> userPosts;

  //оценки, данные пользователем
  @OneToMany(mappedBy = "userVote", orphanRemoval = true)
  @LazyCollection(LazyCollectionOption.EXTRA)
  private List<PostVote> userPostVotes;

  //комментарии пользователя
  @OneToMany(mappedBy = "commentUser", orphanRemoval = true)
  @LazyCollection(LazyCollectionOption.EXTRA)
  private List<PostComment> userPostComments;

  //список ролей пользователя
  @Transient
  private Set<Role> roles = new HashSet<>();

  //конструктор для регистрации
  public User(String email, String name, String password) {
    this.email = email;
    this.name = name;
    this.password = password;
    this.isModerator = 0;
    setRegTime(Instant.now().toEpochMilli());
    setRoles();
  }

  //get timestamp in seconds
  public long getTimestamp() {
    return regTime.getTime() / 1000;
  }

  //timestamp in milliseconds to java.util.Date
  public void setRegTime(long timestamp) {
    regTime = Date.from(Instant.ofEpochMilli(timestamp));
  }

  public void setRoles() {
    roles.add(new Role(Roles.USER));
    if (isModerator == 1) {
      roles.add(new Role(Roles.MODERATOR));
    }
  }

  public Set<Role> getRoles() {
    if (roles.size() == 0) {
      setRoles();
    }
    return roles;
  }

  public void setRoles(Set<Role> set) {
    setRoles();
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