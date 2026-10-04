package com.almacen.stock_flow;

import java.util.List;
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

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"nombre\":\"Producto de prueba\",\"categoria\":\"Pruebas\",\"unidadMedida\":\"UDS\",\"cantidad\":10,\"stockMinimo\":2,\"proveedor\":\"Proveedor Prueba\"}"))
				.andExpect(status().isCreated());

		String codigoGenerado = productoRepository.findAll().stream()
				.filter(p -> p.getNombre().equalsIgnoreCase("Producto de prueba")).findFirst().orElseThrow().getCodigo();

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
	void secondEntryForSameProductSumsStockInsteadOfDuplicating() throws Exception {
		Usuario operador = usuarioRepository.save(new Usuario("operador_reingreso", "Operador Reingreso",
				passwordEncoder.encode("clave-segura-2026")));

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"nombre\":\"Tornillo hexagonal\",\"categoria\":\"Ferreteria\",\"unidadMedida\":\"UDS\",\"cantidad\":10,\"stockMinimo\":2,\"proveedor\":\"Acme\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"nombre\":\"tornillo HEXAGONAL\",\"categoria\":\"ferreteria\",\"unidadMedida\":\"uds\",\"cantidad\":5,\"stockMinimo\":2,\"proveedor\":\"ACME\"}"))
				.andExpect(status().isCreated());

		List<Producto> productos = productoRepository.findAll().stream()
				.filter(p -> p.getNombre().equalsIgnoreCase("Tornillo hexagonal")).toList();
		org.hamcrest.MatcherAssert.assertThat(productos, org.hamcrest.Matchers.hasSize(1));
		org.hamcrest.MatcherAssert.assertThat(productos.get(0).getStockActual(), org.hamcrest.Matchers.equalTo(15));
	}

	@Test
	void inventoryRejectsInvalidSessionToken() throws Exception {
		mockMvc.perform(get("/api/productos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void productCodeIsGeneratedFromNameCategorySupplierAndAvoidsPrefixCollisions() throws Exception {
		Usuario operador = usuarioRepository.save(new Usuario("operador_codigo", "Operador Codigo",
				passwordEncoder.encode("clave-segura-2026")));

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"nombre\":\"Valvula Industrial\",\"categoria\":\"Hidraulica\",\"unidadMedida\":\"UDS\",\"cantidad\":10,\"stockMinimo\":2,\"proveedor\":\"Zeta\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/movimientos/entrada")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + operador.getTokenSesion())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"nombre\":\"Vagon Industrial\",\"categoria\":\"Hidraulica\",\"unidadMedida\":\"UDS\",\"cantidad\":5,\"stockMinimo\":2,\"proveedor\":\"Zeta\"}"))
				.andExpect(status().isCreated());

		Producto primero = productoRepository.findAll().stream()
				.filter(p -> p.getNombre().equalsIgnoreCase("Valvula Industrial")).findFirst().orElseThrow();
		Producto segundo = productoRepository.findAll().stream()
				.filter(p -> p.getNombre().equalsIgnoreCase("Vagon Industrial")).findFirst().orElseThrow();

		String prefijoEsperado = "VIHIDZET";
		org.hamcrest.MatcherAssert.assertThat(primero.getCodigo(),
				org.hamcrest.Matchers.matchesPattern(prefijoEsperado + "-\\d{4}"));
		org.hamcrest.MatcherAssert.assertThat(segundo.getCodigo(),
				org.hamcrest.Matchers.matchesPattern(prefijoEsperado + "-\\d{4}"));
		org.hamcrest.MatcherAssert.assertThat(segundo.getCodigo(),
				org.hamcrest.Matchers.not(org.hamcrest.Matchers.equalTo(primero.getCodigo())));
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
