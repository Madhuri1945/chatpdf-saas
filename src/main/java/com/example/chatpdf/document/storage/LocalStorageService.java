package com.example.chatpdf.document.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService{

    private static final Path UPLOAD_DIR= Paths.get("uploads");
    @Override
    public String store(MultipartFile file){

        try{
            Files.createDirectories(UPLOAD_DIR);
            String extension=".pdf";
            String uniqueFilename= UUID.randomUUID()+extension;

            Path filePath=UPLOAD_DIR.resolve(uniqueFilename);
            Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();

        }catch(Exception e){
            throw new RuntimeException("File storage failed");
        }
    }

    @Override
    public void delete(String storagePath){

    }
}
