package com.gobtx.frontend.ws.config.servlet;

import cn.hutool.json.JSONUtil;
import com.gobtx.common.web.ResultGenerator;
import com.gobtx.common.web.spring.JwtAuthenticationTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Profile("servlet")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class ServletSecurityConfig extends WebSecurityConfigurerAdapter {

    static final Logger logger = LoggerFactory.getLogger(ServletSecurityConfig.class);

    @Value("#{'${security.white.url:/**/logon,/**/register}'.split(',')}")
    List<String> securityWhiteUrls;

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(){
        return new JwtAuthenticationTokenFilter();
    }


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {


        logger.warn(
                "\n\n=============SECURITY_WHITE_URL=============\n\t{}\n\n===============================\n",
                String.join("\n\t", securityWhiteUrls));

        httpSecurity
                .csrf()// base on JWT，csrf
                .disable()
                .sessionManagement() // base on token no session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, // we may not use this
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/swagger-resources/**",
                        "/v2/api-docs/**"
                )
                .permitAll()
                .antMatchers(securityWhiteUrls.toArray(new String[0]))
                .permitAll()
                .antMatchers(HttpMethod.OPTIONS)//跨域请求会先进行一次options请求
                .permitAll()
                /* .antMatchers("/**")//测试时全部运行访问
                 .permitAll()*/
                .anyRequest()//except before all need permission
                .authenticated();
        // VOID CACHE
        httpSecurity.headers().cacheControl();
        // JWT filter
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        //AUTH
        httpSecurity
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {

                    // TODO: 2019/11/8  make this too much generic

                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().println(JSONUtil.parse(ResultGenerator.forbidden(accessDeniedException.getMessage())));
                    response.getWriter().flush();


                })
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().println(JSONUtil.parse(ResultGenerator.unauthorized(authException.getMessage())));
                    response.getWriter().flush();
                });

    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean("userDetailsService")
    public UserDetailsService userDetailsService(){


        // TODO: 2019/11/8 this is mock please remove it to outside

        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return null;
            }
        };
    }


    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(bCryptPasswordEncoder);
    }

}
