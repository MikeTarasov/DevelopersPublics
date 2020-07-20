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

        System.out.println("\tauthenticate =-> " + authentication + "\n" + login + "\n" + password);

        if (userService == null)
            throw new InternalAuthenticationServiceException("UserService is null");


        UserDetails user = userService.loadUserByUsername(login);
        System.out.println("\t\tuser= " + user);

        if (user == null)
            throw new AuthenticationCredentialsNotFoundException("Not found");


        System.out.println("\tauthenticate user -> " + user);

        if (userService.isPasswordCorrect(userService.findUserByLogin(login), password)) {
            System.out.println("return new UsernamePasswordAuthenticationToken" + "\n" + authentication.getCredentials()
            + "\n" + user.getAuthorities());
            return new UsernamePasswordAuthenticationToken(
                    user,
//                    authentication.getCredentials(),
                    userService.encodePassword(password),
                    user.getAuthorities());
        } else {
            System.out.println("Unable to auth against third party systems");
            throw new AuthenticationServiceException("Unable to auth against third party systems");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        System.out.println("\tsupports auth ==>" + authentication);
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
