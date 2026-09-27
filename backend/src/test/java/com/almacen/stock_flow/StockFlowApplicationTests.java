package com.almacen.stock_flow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class StockFlowApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void applicationStartsWithoutDefaultUsers() {
		assertFalse(usuarioRepository.existsByUsuarioIgnoreCase("admin"));
		assertFalse(usuarioRepository.existsByUsuarioIgnoreCase("estudiante"));
		assertFalse(usuarioRepository.existsByUsuarioIgnoreCase("walter"));
	}

	@Test
	void loginAcceptsManuallyStoredUser() throws Exception {
		usuarioRepository.save(new Usuario(
				"usuario_login",
				"Usuario Login",
				passwordEncoder.encode("clave-segura-2026")));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"usuario_login\",\"contrasena\":\"clave-segura-2026\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Usuario Login"));
	}

	@Test
	void registrationPersistsUserAndAllowsLogin() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"cuenta_nueva\",\"nombre\":\"Cuenta Nueva\",\"contrasena\":\"clave-segura-2026\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombre").value("Cuenta Nueva"))
				.andExpect(jsonPath("$.contrasena").doesNotExist());

		Usuario usuarioGuardado = usuarioRepository.findByUsuarioIgnoreCase("cuenta_nueva").orElseThrow();
		assertNotEquals("clave-segura-2026", usuarioGuardado.getContrasena());
		assertTrue(passwordEncoder.matches("clave-segura-2026", usuarioGuardado.getContrasena()));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"CUENTA_NUEVA\",\"contrasena\":\"clave-segura-2026\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Cuenta Nueva"));
	}

	@Test
	void registrationRejectsDuplicateUsernamesIgnoringCase() throws Exception {
		String request = "{\"usuario\":\"cuenta_duplicada\",\"nombre\":\"Cuenta\",\"contrasena\":\"clave-segura-2026\"}";
		mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"CUENTA_DUPLICADA\",\"nombre\":\"Otra Cuenta\",\"contrasena\":\"clave-segura-2026\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void registrationRequiresSecurePassword() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"cuenta_invalida\",\"nombre\":\"Cuenta\",\"contrasena\":\"1234\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void loginRejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuario\":\"admin\",\"contrasena\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginAllowsRequestsFromAngularDevelopmentServer() throws Exception {
		mockMvc.perform(options("/api/auth/login")
				.header(HttpHeaders.ORIGIN, "http://localhost:4200")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
	}

}
