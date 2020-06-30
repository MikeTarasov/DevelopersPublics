package main.com.skillbox.ru.developerspublics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import main.com.skillbox.ru.developerspublics.model.enums.Roles;
import org.springframework.security.core.GrantedAuthority;

@Data
@NoArgsConstructor
public class Role implements GrantedAuthority
{
    private Roles name;

    public Role(Roles roles) {
        name = roles;
    }

    @Override
    public String getAuthority() {
        return getName().getRole();
    }
}
