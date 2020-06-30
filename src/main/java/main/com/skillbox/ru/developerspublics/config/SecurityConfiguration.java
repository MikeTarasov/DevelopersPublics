package main.com.skillbox.ru.developerspublics.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



@Order(1)
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        System.out.println("\t2 - http security");
        httpSecurity
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/").not().fullyAuthenticated()
                .antMatchers("/v2/api-docs", "/swagger-ui.html", "/resources/**").hasRole("MODERATOR")
                .antMatchers("/resources/**").permitAll();

//        httpSecurity
//                .formLogin()
//                .loginPage("/login")
//                .permitAll()
//                .successForwardUrl("/")
//                .and()
//                .logout()
//                .permitAll()
//                .logoutSuccessUrl("/");
    }




    //заставляем Spring использовать кодировщик BCrypt для хеширования и сравнения паролей
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        System.out.println("\t4 - BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService getUserDetailsService(){
        System.out.println("\t5 - getUserDetailsService()");
        return new UserService();
    }

    //мы хотим использовать UserService для нашей аутентификации
    @Override
    public void configure(AuthenticationManagerBuilder builder)
            throws Exception {
        System.out.println("\t1 - AuthenticationManagerBuilder");
        builder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }


    //скрываем содержимое view от пользователей
    @Override
    public void configure(WebSecurity web) throws Exception {
        System.out.println("\t3 - WebSecurity");
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }
}
