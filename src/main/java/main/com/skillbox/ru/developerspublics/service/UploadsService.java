package main.com.skillbox.ru.developerspublics.service;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostCommentsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UploadsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


@Service
public class UploadsService {
    @Autowired
    private UploadsRepository uploadsRepository;

    @Autowired
    private PostCommentsRepository postCommentsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${avatar.path}")
    private String avatarPath;

    @Value("${uploads.path}")
    private String uploadsPath;

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


    @Scheduled(fixedRateString = "${images.auto.clean.time}")
    public void autoCleanUnusedImages() {
        new Thread(this::cleanUnusedImages).start();
    }

    @SneakyThrows
    private void cleanUnusedImages() {
        List<Uploads> oldUploads = new ArrayList<>();

        for (Uploads upload : uploadsRepository.findAll()) {
            //check avatars
            if (upload.getPath().contains(avatarPath)) {
                String path = upload.getPath().substring(upload.getPath().indexOf(avatarPath));
                User user = usersRepository.findByPhotoContaining(path);
                if (user == null) oldUploads.add(upload);
            }
            else {  //check uploads
                if (upload.getPath().contains(uploadsPath)) {
                    String path = upload.getPath().substring(upload.getPath().indexOf(uploadsPath));
                    Post post = postsRepository.findByTextContaining(path);
                    PostComment postComment = postCommentsRepository.findByTextContaining(path);
                    if (post == null && postComment == null) oldUploads.add(upload);
                }
            }
        }

        if (oldUploads.size() != 0) {
            for (Uploads upload : oldUploads) {
                String path = upload.getPath();
                uploadsRepository.delete(upload);
                new File(path).delete();
            }
        }
    }
}
