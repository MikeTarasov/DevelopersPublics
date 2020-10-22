package main.com.skillbox.ru.developerspublics.config;


import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private AuthenticationProviderImpl authenticationProvider;

  @Resource(name = "authenticationProviderImpl")
  public void setAuthenticationProvider(AuthenticationProviderImpl authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
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
        .logoutSuccessUrl("/")
        .and()
        .authorizeRequests()
        .antMatchers("/login/change-password/*").anonymous();
  }


  //мы хотим использовать AuthenticationProviderImpl для нашей аутентификации
  @Override
  public void configure(AuthenticationManagerBuilder builder) {
    builder.authenticationProvider(authenticationProvider);
  }


  //скрываем содержимое view от пользователей
  @Override
  public void configure(WebSecurity web) {
    web
        .ignoring()
        .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
  }
}