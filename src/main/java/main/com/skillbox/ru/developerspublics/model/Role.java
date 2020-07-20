package main.com.skillbox.ru.developerspublics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import org.springframework.security.core.GrantedAuthority;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority
{
    private Roles role;

    @Override
    public String getAuthority() {
        return role.getRole();  //ROLE_USER
    }
}
