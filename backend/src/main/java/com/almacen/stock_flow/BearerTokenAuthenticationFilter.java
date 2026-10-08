package com.almacen.stock_flow;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
  private final UsuarioService usuarioService;

  public BearerTokenAuthenticationFilter(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      try {
        Usuario usuario = usuarioService.buscarActivoPorToken(authorization);
        var authentication = new UsernamePasswordAuthenticationToken(
            usuario.getUsuario(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (ResponseStatusException exception) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}
