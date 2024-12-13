package com.example.RDSExample.controller;

import com.example.RDSExample.model.FileDownloadModel;
import com.example.RDSExample.model.FileInfo;
import com.example.RDSExample.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping()
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> UploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestHeader("db-upload") String dbUpload
            ) throws IOException {
        // Handle empty file error
        if (files.length == 0) {
            return CompletableFuture
                    .completedFuture(ResponseEntity.badRequest().body("No files submitted"));
        } else {

            for (MultipartFile file : files) {
                try {
                    if(dbUpload != "" && dbUpload.equals("true")) {
                        fileService.saveToDB(file);
                    } else {
                        fileService.saveToStorage(file);
                    }
                } catch (Exception e) {
                    return CompletableFuture
                            .completedFuture(ResponseEntity.internalServerError().body(e.getMessage()));
                }
            }
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok("File upload started"));
        }

    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> FetchFiles() {
        List<FileInfo> totalFileList = null;

        totalFileList = fileService.loadAllFilesFromStorage().map(path -> {
                String filename = path.getFileName().toString();
                String url = MvcUriComponentsBuilder
                        .fromMethodName(FileController.class, "getFile", path.getFileName().toString()).build().toString();
                return new FileInfo(filename, url);
            }).collect(Collectors.toList());

        List<FileInfo> fileFromDB = fileService.loadAllFilesFromDB().map(dbFile -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .path(dbFile.getId())
                    .toUriString();
            return new FileInfo(
                    dbFile.getName(),
                    fileDownloadUri );
        }).collect(Collectors.toList());
        totalFileList.addAll(fileFromDB);
        return ResponseEntity.status(HttpStatus.OK).body(totalFileList);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        FileDownloadModel file = fileService.load(filename);
        System.out.println(file.getType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getName())
                        .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file.getFile());
    }
}

