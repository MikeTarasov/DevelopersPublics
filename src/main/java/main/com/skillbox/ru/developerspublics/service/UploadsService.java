package main.com.skillbox.ru.developerspublics.service;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
import main.com.skillbox.ru.developerspublics.model.repository.UploadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
        uploadsRepository.save(new Uploads(path, Files.readAllBytes(Path.of(path))));
    }

    public boolean restoreImage(String homePath, String path) {
        try {
            Uploads uploads = uploadsRepository.findByPath(path);
            if (uploads == null) return false;
            FileOutputStream fos = new FileOutputStream(homePath + path);
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
