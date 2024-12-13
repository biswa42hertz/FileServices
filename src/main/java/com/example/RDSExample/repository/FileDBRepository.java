package com.example.RDSExample.repository;

import com.example.RDSExample.model.FileDBModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDBRepository  extends JpaRepository<FileDBModel, String> {

}
