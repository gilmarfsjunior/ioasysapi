package ioasys.api.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import ioasys.api.ApplicationContextLoad;
import ioasys.api.model.Usuario;
import ioasys.api.repository.UsuarioRepository;

@Service
@Component
public class JWTTokenAutenticacaoService {

	// Tempo de expiração do Token
	private static final long EXPIRATION_TIME = 172800000;

	private static final String SECRET = "SenhaExtremamenteSecreta";

	private static final String TOKEN_PREFIX = "Bearer";

	private static final String HEADER_STRING = "Authorization";

	public void addAuthentication(HttpServletResponse response, String username) throws IOException {

		String JWT = Jwts.builder().setSubject(username)// Chama o gerador de token e adiciona o usuario
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))// Determina o tempo de expiração
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();// Compactação e algoritimos de geração

		// Junta o token ao prefixo
		String token = TOKEN_PREFIX + " " + JWT;// Bearer 432l3k4jl32k4hhk4j234k23h4l2k3423k4j

		// Adiciona no cabeçalho Http
		response.addHeader(HEADER_STRING, token);// Authorization:Bearer 432l3k4jl32k4hhk4j234k23h4l2k34
		
		ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
		.atualizaTokenUser(JWT, username);
		
		// Liberando acessos para portas diferentede usarem a API
		liberacaoCors(response);

		// Escreve token como resposta no corpo http em Json
		response.getWriter().write("{\"Authorizantion\": \"" + token + "\"}");

	}

	// Retorna o usuario validado com o token ou caso acesso negado retorna null
	public Authentication getAuthorization(HttpServletRequest request, HttpServletResponse response) {

		// Consulta o token enviado no cabeçalho
		String token = request.getHeader(HEADER_STRING);

		try {

			if (token != null) {

				String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();

				String user = Jwts.parser().setSigningKey(SECRET)// Retorna tudo Bearer
																	// 432l3k4jl32k4hhk4j234k23h4l2k3423k4j
						.parseClaimsJws(tokenLimpo)// Aqui ja retorna apenas 432l3k4jl32k4hhk4j234k23h4l2k3423k4j
						.getBody().getSubject();// Aqui retorna o usuario "João alguma coisa"

				if (user != null) {

					Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
							.findUserByLogin(user);

					if (usuario != null) {

						if (tokenLimpo.equals(usuario.getToken())) {

							return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
									usuario.getAuthorities());

						}

					}

				}

			}

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			
			try {
				response.getOutputStream().println("Seu TOKEN está expirado. Faça um novo Login ou informe um TOKEN válido!");
			} catch (IOException e1) {
				
			}
			
		}

		liberacaoCors(response);

		return null;

	}

	private void liberacaoCors(HttpServletResponse response) {

		if (response.getHeader("Access-Control-Allow-Origin") == null) {

			response.addHeader("Access-Control-Allow-Origin", "*");

		}

		if (response.getHeader("Access-Control-Allow-Headers") == null) {

			response.addHeader("Access-Control-Allow-Headers", "*");

		}

		if (response.getHeader("Access-Control-Request-Headers") == null) {

			response.addHeader("Access-Control-Request-Headers", "*");

		}

		if (response.getHeader("Access-Control-Allow-Methods") == null) {

			response.addHeader("Access-Control-Allow-Methods", "*");

		}

	}

}
