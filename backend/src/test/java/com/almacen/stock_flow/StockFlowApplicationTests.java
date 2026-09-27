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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private InventarioService inventarioService;

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
				.andExpect(jsonPath("$.nombre").value("Usuario Login"))
				.andExpect(jsonPath("$.rol").value("OPERATIVO"));
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

	@Test
	void entryAndExitUpdateInventoryAndRejectInsufficientStock() throws Exception {
		Usuario operador = usuarioRepository.save(new Usuario("operador_stock", "Operador Stock",
				passwordEncoder.encode("clave-segura-2026")));
		String codigoGenerado = inventarioService.siguienteCodigoProducto();

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"codigo\":\"CODIGO_IGNORADO\",\"nombre\":\"Producto de prueba\",\"categoria\":\"Pruebas\",\"unidadMedida\":\"UDS\",\"cantidad\":10,\"stockMinimo\":2}"))
				.andExpect(status().isCreated());

		String producto = mockMvc
				.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion()))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		org.hamcrest.MatcherAssert.assertThat(producto, org.hamcrest.Matchers.containsString(codigoGenerado));

		Long productoId = productoRepository.findByCodigoIgnoreCase(codigoGenerado).orElseThrow().getId();
		mockMvc.perform(post("/api/movimientos/salida")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productoId\":" + productoId + ",\"cantidad\":3,\"motivo\":\"Prueba\",\"destino\":\"Taller\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/movimientos/salida")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productoId\":" + productoId + ",\"cantidad\":99,\"motivo\":\"Prueba\"}"))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/api/reportes/inventario.csv")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion()))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(codigoGenerado)));

		mockMvc.perform(get("/api/reportes/movimientos.csv")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion()))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Producto de prueba")));
	}

	@Test
	void inventoryRejectsInvalidSessionToken() throws Exception {
		mockMvc.perform(get("/api/productos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void nextProductCodeUsesSixDigitSequence() throws Exception {
		Usuario operador = usuarioRepository.save(new Usuario("usuario_codigo", "Usuario Codigo",
				passwordEncoder.encode("clave-segura-2026")));
		String codigoBase = inventarioService.siguienteCodigoProducto();
		productoRepository.save(new Producto(codigoBase, "Producto inicial", "Pruebas", "UDS", 1,
				null, null, null));
		String codigoEsperado = String.format("PROD-%06d", Integer.parseInt(codigoBase.substring(5)) + 1);

		mockMvc.perform(get("/api/productos/siguiente-codigo")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion()))
				.andExpect(status().isOk())
				.andExpect(content().string(codigoEsperado));
	}

	@Test
	void logoutRevokesSessionToken() throws Exception {
		Usuario usuario = usuarioRepository.save(new Usuario("usuario_logout", "Usuario Logout",
				passwordEncoder.encode("clave-segura-2026")));
		String token = usuario.getTokenSesion();

		mockMvc.perform(post("/api/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/productos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

}
