package com.example.RDSExample.service;

import com.example.RDSExample.model.FileDBModel;
import com.example.RDSExample.model.FileDownloadModel;
import com.example.RDSExample.repository.FileDBRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileService {


    @Autowired
    private FileDBRepository fileDBRepository;

    private final Path root = Paths.get("G:\\Springboot_Projects\\FileStorage");

    @SneakyThrows
    @Async
    public void saveToStorage(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public Stream<Path> loadAllFilesFromStorage() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public FileDownloadModel load(String filename) {
        Resource resource = null;
        FileDownloadModel fileDownloadModel = null;
        String type = "";
        String name = "";
        try {
            Path file = root.resolve(filename);
            resource = new UrlResource(file.toUri());
            type = Files.probeContentType(file);
            name = filename;
            if (!resource.exists() || !resource.isReadable()) {
                FileDBModel filefromDB = getFileFromDB(filename);
                resource = new ByteArrayResource(filefromDB.getData());
                type = filefromDB.getType();
                name = filefromDB.getName();
            }
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not available Or could not read the file!");
            }
            return FileDownloadModel.builder()
                    .name(name)
                    .type(type)
                    .file(resource)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public FileDBModel saveToDB(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileDBModel fbdInfo = FileDBModel.builder()
                .name(fileName)
                .type(file.getContentType())
                .data(file.getBytes())
                .build();
        return fileDBRepository.save(fbdInfo);
    }

    public FileDBModel getFileFromDB(String id) {
        return fileDBRepository.findById(id).get();
    }

    public Stream<FileDBModel> loadAllFilesFromDB() {
        return fileDBRepository.findAll().stream();
    }

    public String deleteFile(String filename) {


        return "File deleted succesfully";
    }

    public String deletFileById(String id) {
        if (fileDBRepository.existsById(id)) {
            fileDBRepository.deleteById(id);
            return "File has been successfully deleted";
        }
        return "File doesn't exist";
    }
}
