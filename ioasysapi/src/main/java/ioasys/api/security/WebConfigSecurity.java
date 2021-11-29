package ioasys.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import ioasys.api.service.ImplementacaoUserDetailsService;
//Mapeia URLs, endereços, autoriza ou bloqueia acessos.
@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	//Configura as requisiçoes de acessos por Http
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		//Ativa a proteção contra usuários que não estão validados por token.
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		
		//Ativando permissão para o acesso a pagina inicial(sem necessidade de Loign)
		.disable().authorizeRequests().antMatchers(HttpMethod.GET, "/").permitAll().antMatchers("/index").permitAll()
		
		//Delegando acessos aos usuarios do tipo admin e usuarios de acessos comuns
		.antMatchers(HttpMethod.POST, "/usuario/").hasAnyRole("ADMIN", "USER")
		.antMatchers(HttpMethod.PUT, "/usuario/").hasAnyRole("ADMIN", "USER")
		.antMatchers(HttpMethod.DELETE, "/usuario/{id}").hasAnyRole("ADMIN", "USER")
		
		//Liberando o acesso para portas diferente acessarem a API acesso web
		.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		
		//Configura redirecionamento após o logout do usuário
		.anyRequest().authenticated().and().logout().logoutSuccessUrl("/index")
		
		//Mapeia a URL de logout e invalida o usuário
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		
		//Filtrar requisições de login para autenticação
		.and().addFilterBefore(new JWTLoginFilte("/login", authenticationManager()), 
		 UsernamePasswordAuthenticationFilter.class)
		
		//Filtrar as demais requisições para verificar a presença do TOKEN JWT no HEADER HTTP
		.addFilterBefore(new JwtApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);

	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		//Serviço que irá consultar o usuário no banco de dados.
		auth.userDetailsService(implementacaoUserDetailsService)
		.passwordEncoder(new BCryptPasswordEncoder());
	}

}
