package com.petlogue.duopetbackend.security.config;

import com.petlogue.duopetbackend.security.filter.JWTFilter;
import com.petlogue.duopetbackend.security.filter.LoginFilter;
import com.petlogue.duopetbackend.security.handler.CustomLogoutHandler;
import com.petlogue.duopetbackend.security.handler.CustomOAuth2SuccessHandler;
import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.security.model.service.CustomUserDetailsService;
import com.petlogue.duopetbackend.social.model.service.GoogleService;
import com.petlogue.duopetbackend.social.model.service.KakaoService;
import com.petlogue.duopetbackend.social.model.service.NaverService;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RefreshService refreshService;
    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final GoogleService googleService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final com.petlogue.duopetbackend.user.jpa.repository.VetRepository vetRepository;
    private final com.petlogue.duopetbackend.consultation.model.service.VetProfileService vetProfileService;


    public SecurityConfig(JWTUtil jwtUtil, CustomUserDetailsService userDetailsService,
                          UserRepository userRepository, RefreshService refreshService,
                          KakaoService kakaoService, NaverService naverService,
                          GoogleService googleService,
                          CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
                          com.petlogue.duopetbackend.user.jpa.repository.VetRepository vetRepository,
                          com.petlogue.duopetbackend.consultation.model.service.VetProfileService vetProfileService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.refreshService = refreshService;
        this.kakaoService = kakaoService;
        this.naverService = naverService;
        this.googleService = googleService;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        this.vetRepository = vetRepository;
        this.vetProfileService = vetProfileService;
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler(jwtUtil, refreshService, userRepository, vetRepository, vetProfileService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // CORS 설정
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001") // 배포시 실제 도메인으로 추가
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("token-expired", "Authorization", "RefreshToken")
                .allowCredentials(true);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   CustomLogoutHandler customLogoutHandler, KakaoService kakaoService) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 전문가 상담 공개 API - 인증 불필요 (가장 먼저 설정)
                        .requestMatchers(HttpMethod.GET, 
                                "/api/consultation/vet-profiles/available",
                                "/api/consultation/vet-profiles/online",
                                "/api/consultation/vet-profiles/search",
                                "/api/consultation/vet-profiles/top-rated",
                                "/api/consultation/vet-profiles/test/all",
                                "/api/consultation/vet-profiles/vet/*",
                                "/api/consultation/consultation-reviews/vet/*",
                                "/api/consultation/vet-schedules/vet/*/available"
                        ).permitAll()
                        
                        // WebSocket 경로 - 인증 불필요
                        .requestMatchers(
                                "/ws-consultation/**",
                                "/ws-consultation/info",
                                "/ws-consultation"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/users/check-face").permitAll()
                        .requestMatchers(HttpMethod.POST,"/board/*/write").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/board/*/edit").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/board/*/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/board/*/like/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/board/*/bookmark/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments/insert").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/comments/delete/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments/comment-like/**").authenticated()

                        
                        // 건강 기록 관련 - 인증 필요
                        .requestMatchers("/api/health/**").authenticated()
                        
                        // 전문가 상담 관련 - 나머지는 인증 필요
                        .requestMatchers("/api/consultation/**").authenticated()




                        .requestMatchers(HttpMethod.DELETE, "/notice/{id}", "/faq/{id}" ).hasAuthority("admin")
                        .requestMatchers(HttpMethod.POST, "/faq").hasAuthority("admin")
                        .requestMatchers(HttpMethod.PUT, "/notice/{id}", "/faq/{id}").hasAuthority("admin")



                        // 인증 없이 접근 가능한 경로 목록
                        .requestMatchers(
                                "/login",
                                "/reissue",
                                "/session/check",
                                "/users/check-id",
                                "/users/signup/**",
                                "/users/check-nickname",
                                "/users/check-email",
                                "/oauth2/**",
                                "/social-signup",
                                "/login/oauth2/**",
                                "/oauth2/authorization/kakao",
                                "/oauth2/redirect",
                                "/vet/upload-temp",
                                "/vet/register",
                                "/shelter/upload-temp",
                                "/shelter/register",
                                "/shelter/check",
                                "/pet/register",
                                "/users/face-upload",
                                "/face-login/success",
                                "/mypage/face-images/**",
                                "/mypage/face-delete",
                                "/pet/list/**",
                                "/pet/image/**",
                                "/pet/*",
                                "/api/summary/public",
                                "/sms/send",
                                "/sms/verify",
                                "/users/find-id",
                                "/users/check-user",
                                "/users/check-phone",
                                "/users//reset-password",
                                "/users/check-face",
                                "/api/v1/face-login/verify",
                                "/notice/**",
                                "/board/maintop-liked",
                                "/board/maintop-viewed",
                                "/board/view-count",
                                "/board/*/detail/**",
                                "/board/*/top-liked",
                                "/board/*/top-viewed",
                                "/board/*/list",
                                "/static/board/**",
                                "/comments/view/**",
                                "/api/v1/video-recommend/**",
                                "/favicon.ico",
                                "/faq",
                                "/qna",
                                "/api/shopping/items",
                                "/qna/**",
                                "/api/info/**",
                                "/api/adoption/**",
                                "/api/hospitals/**",
                                "/uploads/**", "/images/**",
                                "/upload/**",
                                
                                // WebSocket endpoints
                                "/ws-consultation/**"


                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST, "/qna" // 문의글 작성
                                // 여기에 다른 로그인 필수 POST 경로 추가 가능
                        ).authenticated()

                        // 로그아웃은 인증 필요
                        .requestMatchers("/logout").authenticated()

                        // 관리자 전용 경로
                        .requestMatchers("/admin/**").hasAuthority("admin")

                        // 그 외는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                .authenticationProvider(daoAuthenticationProvider())

                // JWT 필터 등록
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)

                // 로그인 필터 등록
                .addFilterAt(
                        new LoginFilter(authenticationManager, jwtUtil, userRepository, refreshService, 
                                vetRepository, vetProfileService),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 소셜 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(userRequest -> {
                                    String registrationId = userRequest.getClientRegistration().getRegistrationId();
                                    if ("kakao".equals(registrationId)) {
                                        return kakaoService.loadUser(userRequest);
                                    } else if ("naver".equals(registrationId)) {
                                        return naverService.loadUser(userRequest);
                                    } else if ("google".equals(registrationId)) {
                                        return googleService.loadUser(userRequest);
                                    } else {
                                        throw new IllegalArgumentException("Unknown OAuth2 provider: " + registrationId);
                                    }
                                })
                        )
                        .successHandler(customOAuth2SuccessHandler)
                )


                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("로그아웃 성공");
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
