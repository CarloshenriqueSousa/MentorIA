package com.mentoria.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @Column(unique = true, nullable = false)
    private String Email;

    @Column(unique = true, nullable = false)
    private String Name_user;

    @Column(name = "password_hash", unique = true)
    private String Password_Hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    @Builder.Default
    public PlanType Plantype = PlanType.FREE;

    @Column(name = "activated", nullable = false)
    @Builder.Default
    private Boolean Activated = true;

    @Column(name = "stripe_customer_id")
    private String StripeCustomerId;

    @Column(name = "last_login_at")
    private String LastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime CreatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime UpdatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile Profile;

    public enum PlanType {
        FREE, PRO, PREMIUM;
    }
}
