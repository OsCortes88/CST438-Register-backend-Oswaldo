package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cst438.dto.AccountCredentials;
import com.cst438.service.JwtService;


@RestController
public class LoginController {
	@Autowired
	private JwtService jwtService;

	@Autowired	
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@RequestMapping(value="/login", method=RequestMethod.POST)
	public ResponseEntity<String> getToken(@RequestBody AccountCredentials credentials) {
		UsernamePasswordAuthenticationToken creds =
				new UsernamePasswordAuthenticationToken(
						credentials.username(),
						credentials.password());

		Authentication auth = authenticationManager.authenticate(creds);

		// Generate token
		String jwts = jwtService.getToken(auth.getName());
		User user = userRepository.findByEmail(credentials.username());
		String role = null;
		if (user != null) {
			role = user.getRole();
		}

		// Build response with the generated token and role of the user
		return ResponseEntity.ok()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts)
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization")
				.body(asJsonString(role));

	}
	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
