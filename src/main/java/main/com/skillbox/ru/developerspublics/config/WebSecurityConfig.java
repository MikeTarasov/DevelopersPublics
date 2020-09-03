package main.com.skillbox.ru.developerspublics.config;


import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationProviderImpl authenticationProvider;

    //заставляем Spring использовать кодировщик BCrypt для хеширования и сравнения паролей
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .formLogin()
                .loginPage("/login")
                .usernameParameter("e_mail")
                .passwordParameter("password")
                .successForwardUrl("/")
                .and()
                .logout()
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/")  //TODO don't work!!!!
                .and()
                .authorizeRequests()
                .antMatchers("/v2/api-docs", "/swagger-ui.html").hasRole("MODERATOR")
                .antMatchers("/login/change-password/*").anonymous();
    }

    //мы хотим использовать AuthenticationProviderImpl для нашей аутентификации
    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.authenticationProvider(authenticationProvider);
    }

    //скрываем содержимое view от пользователей
    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }
}
