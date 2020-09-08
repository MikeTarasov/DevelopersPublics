package main.com.skillbox.ru.developerspublics.config;


import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;


@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {
    @Autowired
    private UserService userService;


    @Resource(name="userService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (userService == null)
            throw new InternalAuthenticationServiceException("UserService is null");

        UserDetails user = userService.loadUserByUsername(login);

        if (user == null)
            throw new AuthenticationCredentialsNotFoundException("Not found");

        if (userService.isPasswordCorrect(userService.findUserByLogin(login), password)) {
            return new UsernamePasswordAuthenticationToken(
                    user,
                    userService.encodePassword(password),
                    user.getAuthorities());
        } else {
            throw new AuthenticationServiceException("Unable to auth against third party systems");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}