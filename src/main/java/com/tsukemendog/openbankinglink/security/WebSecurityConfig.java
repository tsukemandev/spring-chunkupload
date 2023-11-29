package com.tsukemendog.openbankinglink.security;

import com.tsukemendog.openbankinglink.security.filter.Test2Filter;
import com.tsukemendog.openbankinglink.security.filter.TestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /* 모든 uri 에 대하여 접근 권한 요구 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize.mvcMatchers("/**").permitAll()) //.authenticate() 로 변경
                .formLogin().disable()
                .httpBasic().disable()
                .addFilterBefore(new TestFilter(), AuthorizationFilter.class);
        return http.build();
    }

    /* 폼 로그인 필터 테스트용 */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
