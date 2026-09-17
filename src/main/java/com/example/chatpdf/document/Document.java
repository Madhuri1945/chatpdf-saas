package com.example.chatpdf.document;

import com.example.chatpdf.auth.User;
import jakarta.persistence.*;

@Entity
@Table(name="document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
}
