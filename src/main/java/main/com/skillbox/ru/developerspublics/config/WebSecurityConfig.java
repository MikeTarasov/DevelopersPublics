package main.com.skillbox.ru.developerspublics.config;


import main.com.skillbox.ru.developerspublics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



//@Order(1)
@Configuration
@EnableWebSecurity
//@EnableConfigurationProperties
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserService userService;

    @Autowired AuthenticationProviderImpl authenticationProvider;

    //заставляем Spring использовать кодировщик BCrypt для хеширования и сравнения паролей
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        System.out.println("\t4 - BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        System.out.println("\t2 - http security");
//        httpSecurity
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/").not().fullyAuthenticated()
//                .antMatchers("/v2/api-docs", "/swagger-ui.html", "/resources/**").hasRole("MODERATOR")
//                .antMatchers("/resources/**").permitAll();
//
        httpSecurity
                .csrf().disable()
                .formLogin()
                .loginPage("/login")
                .usernameParameter("e_mail")
                .passwordParameter("password")
                .successForwardUrl("/")
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);
//        httpSecurity
//                .authorizeRequests()
//                .antMatchers("/resources/**", "/registration").permitAll() //TODO resources зачем показываем?
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .permitAll()
//                .and()
//                .logout()
//                .permitAll();
    }

//    @Bean
//    public AuthenticationManager customAuthenticationManager() throws Exception {
//        System.out.println("\t - customAuthenticationManager()");
//        return authenticationManager();
//    }


    //мы хотим использовать UserService для нашей аутентификации
    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        System.out.println("\t1 - AuthenticationManagerBuilder");
//        builder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
        builder.authenticationProvider(authenticationProvider);
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
