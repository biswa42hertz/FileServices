package com.example.RDSExample.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.core.io.Resource;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileDownloadModel {

    private String name;

    private String type;

    private Resource file;

}
