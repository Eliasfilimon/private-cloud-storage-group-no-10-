package com.udom.securecloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Folder name is required")
    @Column(nullable = false)
    private String folderName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> subFolders = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<FileMetadata> files = new ArrayList<>();

    @Column(name = "folder_color")
    private String folderColor = "#4A90E2";

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
