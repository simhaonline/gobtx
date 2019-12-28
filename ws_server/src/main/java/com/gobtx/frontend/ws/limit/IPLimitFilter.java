package com.gobtx.frontend.ws.limit;

import com.gobtx.common.web.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Component
public class IPLimitFilter extends OncePerRequestFilter {

    static final Logger logger = LoggerFactory.getLogger(IPLimitFilter.class);


    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        //Bucket
        // TODO: 2019/11/8  limit the access frequent of the API access

        //Some domain Or Some IPs

        final String ip = NetworkUtil.getIpAddress(request);

        if (logger.isDebugEnabled()) {
            logger.debug("TRY_FILTER {},{}", ip, request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
