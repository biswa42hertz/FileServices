package com.example.RDSExample.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "files")
public class FileDBModel {

    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String type;

    @Lob
    private byte[] data;

}