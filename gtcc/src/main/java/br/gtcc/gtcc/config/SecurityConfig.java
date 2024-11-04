package br.gtcc.gtcc.config;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.gtcc.gtcc.config.handlers.LoginInterceptor;
import br.gtcc.gtcc.model.UserType;
import br.gtcc.gtcc.model.mysql.Usuario;
import br.gtcc.gtcc.services.impl.mysql.UsuarioServices;
import br.gtcc.gtcc.util.JWTUtil;
import br.gtcc.gtcc.util.services.UsuarioUtil;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@Slf4j
public class SecurityConfig implements CommandLineRunner, WebMvcConfigurer {
    
    @Autowired
    private UsuarioUtil userUtil;

    @Autowired
    private UsuarioServices userServices;

    @Autowired
    JWTUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private void addUsers() {
        try {
            // Verifica se o usuário Admin já existe no banco de dados
            Optional<Usuario> existingAdmin = Optional.ofNullable(userUtil.findByLogin("admin"));
            if (existingAdmin.isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Admin");
                admin.setMatricula("0001");
                admin.setLogin("admin");
                admin.setEmail("admin@gmail.com");
                admin.setSenha(passwordEncoder().encode("1234"));
                admin.setTelefone("(82) 98578-4853");
                admin.setDataNascimento("1980-01-01");
                admin.setAtivo(1);
                admin.getPermissoes().add("ROLE_USER");
                admin.getPermissoes().add("ROLE_ADMIN");
                admin.getPermissoes().add("ROLE_PROFESSOR");
                admin.getPermissoes().add("ROLE_COORDENADOR");
                admin.getPermissoes().add("ROLE_ALUNO");
                userUtil.salvarUser(admin);
            }

            // Verifica se o usuário Professor já existe no banco de dados
            Optional<Usuario> existingProfessor = Optional.ofNullable(userUtil.findByLogin("professor"));
            if (existingProfessor.isEmpty()) {
                Usuario professor = new Usuario();
                professor.setNome("Professor");
                professor.setMatricula("0002");
                professor.setLogin("professor");
                professor.setEmail("professor@gmail.com");
                professor.setSenha(passwordEncoder().encode("1234"));
                professor.setTelefone("(31) 2852-0527");
                professor.setDataNascimento("1985-01-01");
                professor.setAtivo(1);
                professor.getPermissoes().add("ROLE_USER");
                professor.getPermissoes().add("ROLE_PROFESSOR");
                userUtil.salvarUser(professor);
            }

            // Verifica se o usuário Coordenador já existe no banco de dados
            Optional<Usuario> existingCoordinator = Optional.ofNullable(userUtil.findByLogin("coordenador"));
            if (existingCoordinator.isEmpty()) {
                Usuario coordenador = new Usuario();
                coordenador.setNome("Coordenador");
                coordenador.setMatricula("0003");
                coordenador.setLogin("coordenador");
                coordenador.setEmail("coordenador@gmail.com");
                coordenador.setSenha(passwordEncoder().encode("1234"));
                coordenador.setTelefone("(31) 98252-0527");
                coordenador.setDataNascimento("1990-01-01");
                coordenador.setAtivo(1);
                coordenador.getPermissoes().add("ROLE_USER");
                coordenador.getPermissoes().add("ROLE_COORDENADOR");
                userUtil.salvarUser(coordenador);
            }

            // Verifica se o usuário Aluno já existe no banco de dados
            Optional<Usuario> existingAluno = Optional.ofNullable(userUtil.findByEmail("aluno@gmail.com"));
            if (existingAluno.isEmpty()) {
                Usuario aluno = new Usuario();
                aluno.setNome("Aluno");
                aluno.setMatricula("0004");
                aluno.setLogin("aluno");
                aluno.setEmail("aluno@gmail.com");
                aluno.setSenha(passwordEncoder().encode("1234"));
                aluno.setTelefone("(82) 3994-1082");
                aluno.setDataNascimento("1995-01-01");
                aluno.setAtivo(1);
                aluno.getPermissoes().add("ROLE_USER");
                aluno.getPermissoes().add("ROLE_ALUNO");
                userUtil.salvarUser(aluno);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        addUsers();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf().disable(); // Desativa a proteção CSRF, se necessário

        return http.build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(userServices, null, jwtUtil))
                .excludePathPatterns("/error**", "/index**", "/doc**", "/auth**", "/swagger-ui**")
                .addPathPatterns("/coordenacao/tcc/v1/apresentacao",
                        "/coordenacao/tcc/v1/apresentacao/**",
                        "/coordenacao/tcc/v1/apresentacoes",
                        "/coordenacao/tcc/v1/coordenador/alunos",
                        "/coordenacao/tcc/v1/coordenador/professor",
                        "/coordenacao/tcc/v1/coordenador/professores",
                        "/coordenacao/tcc/v1/coordenador/usuario/**",
                        "/coordenacao/tcc/v1/Professor/usuario/",
                        "/coordenacao/tcc/v1/Professor/alunos",
                        "/coordenacao/tcc/v1/Professor/aluno",
                        "/coordenacao/tcc/v1/Professor/aluno/**",
                        "/coordenacao/tcc/v1/Professor/",
                        "/coordenacao/tcc/v1/agenda/**",
                        "/coordenacao/tcc/v1/agenda",
                        "/coordenacao/tcc/v1/agendas",
                        "/coordenacao/tcc/v1/tcc",
                        "/coordenacao/tcc/v1/tccs",
                        "/coordenacao/tcc/v1/tcc/**",
                        "/coordenacao/tcc/v1/usuario/**",
                        "/coordenacao/tcc/v1/usuario",
                        "/coordenacao/tcc/v1/usuarios"
                        );
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    }
}