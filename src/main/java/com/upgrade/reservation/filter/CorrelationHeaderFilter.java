package com.upgrade.reservation.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class CorrelationHeaderFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String currentCorrId = httpServletRequest.getHeader(RequestCorrelation.CORRELATION_ID);

		if (currentCorrId == null) {
			currentCorrId = UUID.randomUUID().toString();
			log.info("No correlationId found in Header. Generated : " + currentCorrId);
		} else {
			log.info("Found correlationId in Header : " + currentCorrId);
		}
		RequestCorrelation.setId(currentCorrId);
		chain.doFilter(request, response);
	}

}
