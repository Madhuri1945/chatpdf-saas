package com.example.chatpdf.auth;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="refresh_tokens")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length=500)
    private String token;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Column(nullable=false)
    private UUID familyId;

    @Column(nullable = false)
    private LocalDateTime expiry;

    private boolean revoked;
    @Column(nullable = false,updatable=false)
    private LocalDateTime createdAt;

    public void onCreate(){
        createdAt=LocalDateTime.now();
    }
}
