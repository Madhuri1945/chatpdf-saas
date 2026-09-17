package com.example.chatpdf.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository repo;
    private final CustomerDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository repo,PasswordEncoder encoder,AuthenticationManager authenticationManager,CustomerDetailsService userDetailsService,
                       JwtService jwtService,RefreshTokenService refreshTokenService){
        this.repo=repo;
        this.encoder=encoder;
        this.authenticationManager=authenticationManager;
        this.userDetailsService=userDetailsService;
        this.jwtService=jwtService;
        this.refreshTokenService=refreshTokenService;
    }

    public void register(RegisterRequest request){
        if(repo.existsByEmail(request.email())){
            throw new RuntimeException("Email already exists");
        }
        User user=new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setProvider(Provider.LOCAL);
        repo.save(user);
    }
    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
       User user=repo.findByEmail(request.email()).orElseThrow(()->
               new RuntimeException("user not found"));
        String token=jwtService.generateAccessToken(user);
        RefreshToken refreshToken=refreshTokenService.createRefreshToken(user);
        return new AuthResponse(token,refreshToken.getToken(),refreshToken.getFamilyId());

    }
}
