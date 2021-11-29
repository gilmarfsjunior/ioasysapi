package ioasys.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ioasys.api.model.Usuario;
import ioasys.api.repository.UsuarioRepository;

@Service
public class ImplementacaoUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Usuario usuario = usuarioRepository.findUserByLogin(username);
		
		if(username == null) {
			
			throw new UsernameNotFoundException("O usuario não foi encontrado (Implementação Service)");
			
		}
		
		return new User(usuario.getLogin(), usuario.getPassword(), usuario.getAuthorities());
	}

}
