package com.oneship.chargebook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.oneship.chargebook.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/register", "/login", "/h2-console/**").permitAll() // 로그인 및 회원가입, H2 콘솔은 인증 없이 접근 가능
                .anyRequest().authenticated() // 다른 모든 요청은 인증 필요
                .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // 로그인 성공 시 메인 페이지로 리다이렉션
                .failureUrl("/login?error=true") // 로그인 실패 시 에러 파라미터 추가
                .permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/login?logout=true") // 로그아웃 성공 시 메시지 표시
                .permitAll();

        // H2 Console 설정 추가
        http.csrf().disable();
        http.headers().frameOptions().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
