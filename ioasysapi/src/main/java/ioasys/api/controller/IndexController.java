package ioasys.api.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ioasys.api.model.Usuario;
import ioasys.api.model.UsuarioDTO;
import ioasys.api.repository.UsuarioRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/usuario")
public class IndexController {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<UsuarioDTO> init(@PathVariable(value = "id") Long id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);

	}
	
	@CacheEvict(value = "cacheUsuario", allEntries = true)
	@CachePut("cacheUsuario")
	@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<Page<Usuario>> usuarios() {
		
		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);

		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);

	}
	
	@CachePut("cacheUsuario")
	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	public ResponseEntity<Page<Usuario>> usuariosPagina(@PathVariable("pagina") int pagina) {
		
		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);

		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);

	}
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) throws Exception {

		for (int pos = 0; pos < usuario.getTelefone().size(); pos++) {

			usuario.getTelefone().get(pos).setUsuario(usuario);

		}
		
		if(usuario.getCep() != null) {
		
		//Consumindo API via CEP
		
		URL url = new URL("https://viacep.com.br/ws/" + usuario.getCep() + "/json/");
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		
		String cep = "";
		
		StringBuilder jsonCep = new StringBuilder();
		
		while ((cep = br.readLine()) != null) {
			
			jsonCep.append(cep);
			
		}
		
		Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
		
		usuario.setCep(userAux.getCep());
		usuario.setLogradouro(userAux.getLogradouro());
		usuario.setComplemento(userAux.getComplemento());
		usuario.setBairro(userAux.getBairro());
		usuario.setLocalidade(userAux.getLocalidade());
		usuario.setUf(userAux.getUf());
		
		}
		
		
		String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhaCriptografada);

		Usuario sauvarUsuario = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(sauvarUsuario, HttpStatus.OK);

	}

	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {

		for (int pos = 0; pos < usuario.getTelefone().size(); pos++) {

			usuario.getTelefone().get(pos).setUsuario(usuario);

		}
		
		Usuario userTemporario = usuarioRepository.findUserByLogin(usuario.getLogin());
		
		if(!userTemporario.getSenha().equals(usuario.getSenha())) {
			
			String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhaCriptografada);
			
		}

		Usuario atualizarUsuario = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(atualizarUsuario, HttpStatus.OK);

	}

	@DeleteMapping(value = "/{id}", produces = "application/json")
	public String deletar(@PathVariable("id") Long id) {

		usuarioRepository.deleteById(id);

		return "Usuario deletado!";

	}

}
