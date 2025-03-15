package com.melnikov.TicketBookingService;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TicketBookingServiceApplicationTests {
	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access.expiration}")
	private long accessExpiration;

	@Value("${jwt.refresh.expiration}")
	private long refreshExpiration;

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}


	@Test
	void contextLoads() {

		assertTrue(validateToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpdmFuLnBldHJvdk9yaWdpbmFsQGV4YW1wbGUuY29tIiwiaWQiOjgsInJvbGVJZCI6MiwiaWF0IjoxNzQxNTEwODcxLCJleHAiOjE3NDE1MTE3NzF9.uKlFPbKQFtIcb00rTMRv8zefGz2HZ_Xl3_TD_RX3jrQ"));
	}

}
