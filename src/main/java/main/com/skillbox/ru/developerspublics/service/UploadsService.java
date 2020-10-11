package main.com.skillbox.ru.developerspublics.service;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
import main.com.skillbox.ru.developerspublics.model.repository.UploadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class UploadsService {
    @Autowired
    private UploadsRepository uploadsRepository;

    public void deleteImage(String path) {
        uploadsRepository.delete(uploadsRepository.findByPath(path));
    }

    @SneakyThrows
    public void saveImage(String path) {
        Uploads upload = uploadsRepository.findByPath(path);
        if (upload == null) {
            uploadsRepository.save(new Uploads(path, Files.readAllBytes(Path.of(path))));
        }
        else {
            upload.setBytes(Files.readAllBytes(Path.of(path)));
            uploadsRepository.save(upload);
        }
    }

    public boolean restoreImage(String path, String name) {
        try {
            String filePath = String.join(File.separator, path, name);
            Uploads uploads = uploadsRepository.findByPath(filePath);
            if (uploads == null) return false;

            new File(path).mkdirs();
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(uploads.getBytes());
            fos.flush();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
