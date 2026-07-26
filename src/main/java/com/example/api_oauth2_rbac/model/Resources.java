package com.example.api_oauth2_rbac.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resources")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Resources {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resources_seq")
    @SequenceGenerator(name = "resource_seq", sequenceName = "resource_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Long ownerIs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @OneToMany(mappedBy = "resource")
    private Set<ResourceSharedWithPermission> sharedWithUsers = new HashSet<>();

    protected Resources(Long id, String name, String type, Long ownerIs, User owner,
                        LocalDateTime createdAt, Visibility visibility,
                        Set<ResourceSharedWithPermission> sharedWithUsers) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerIs = ownerIs;
        this.owner = owner;
        this.createdAt = createdAt;
        this.visibility = visibility;
        this.sharedWithUsers = sharedWithUsers;
    }

    protected Resources() {

    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE,
        PROTECTED,
        GROUP,
        READONLY,
    }
}
