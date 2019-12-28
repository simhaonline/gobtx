package com.gobtx.common.web.spring;

import com.gobtx.common.web.utils.JwtTokenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Aaron Kuai on 2019/11/7.
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService userDetailsService;

    @Value("${jwt.tokenHeader:Authorization}")
    private String tokenHeader;

    @Value("${jwt.tokenHead:Bearer}")
    private String tokenHead;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader(this.tokenHeader);
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {

            String authToken = authHeader.substring(this.tokenHead.length());// The part after "Bearer "
            final JwtTokenHelper.JWTResult jwtResult = jwtTokenHelper.parse(authToken, secret);

            if (jwtResult.isValid()) {

                final String username = jwtTokenHelper.getUsername(jwtResult);

                if (logger.isInfoEnabled())
                    logger.info("checking username:{}", username);

                if (username != null &&
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication() == null) {


                    final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {

                        // TODO: 2019/11/8  here is not totally right

                        final UsernamePasswordAuthenticationToken
                                authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetails(request));

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);

                    }


                    /*if (userDetails != null) {

                        if (bCryptPasswordEncoder.matches(userDetails.getUsername() + "," + userDetails.getPassword(), JWTUtils.getPassword())) {
                            if (userDetails instanceof CustomerDetails) {
                                CustomerDetails customerDetails = (CustomerDetails) userDetails;
                                customerKeyService.addAlias(
                                        Arrays.asList(
                                                customerDetails.getPhoneNumber(),
                                                customerDetails.getCustomerId() + ""
                                        ),
                                        customerDetails.getUsername()
                                );
                            }
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            if (LOGGER.isDebugEnabled())
                                LOGGER.info("authenticated user:{}", username);

                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        }
                    }*/
                }
            }
        }
        chain.doFilter(request, response);
    }
}
