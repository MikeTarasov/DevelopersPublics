package main.com.skillbox.ru.developerspublics.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.PostCommentsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UploadsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class UploadsService {

  private final UploadsRepository uploadsRepository;
  private final PostCommentsRepository postCommentsRepository;
  private final PostsRepository postsRepository;
  private final UsersRepository usersRepository;

  @Value("${avatar.path}")
  private String avatarPath;

  @Value("${uploads.path}")
  private String uploadsPath;

  public UploadsService(
      UploadsRepository uploadsRepository,
      PostCommentsRepository postCommentsRepository,
      PostsRepository postsRepository,
      UsersRepository usersRepository) {
    this.uploadsRepository = uploadsRepository;
    this.postCommentsRepository = postCommentsRepository;
    this.postsRepository = postsRepository;
    this.usersRepository = usersRepository;
  }

  public void deleteImage(String path) {
    Uploads upload = uploadsRepository.findByPath(path);
    if (upload != null) {
      uploadsRepository.delete(upload);
    }
  }

  @SneakyThrows
  public void saveImage(String homePath, String path) {
    Uploads upload = uploadsRepository.findByPath(path);
    if (upload == null) {
      uploadsRepository.save(new Uploads(path, Files.readAllBytes(Path.of(homePath + path))));
    } else {
      upload.setBytes(Files.readAllBytes(Path.of(homePath + path)));
      uploadsRepository.save(upload);
    }
  }

  public boolean restoreImage(String homePath, String path, String name) {
    try {
      String pathDB = path + name;
      String pathServer = homePath + pathDB;
      Uploads uploads = uploadsRepository.findByPath(pathDB);
      if (uploads == null) {
        return false;
      }

      new File(homePath + path).mkdirs();
      FileOutputStream fos = new FileOutputStream(pathServer); //TODO
      fos.write(uploads.getBytes());
      fos.flush();
      fos.close();
    } catch (Exception e) {
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
        String path = upload.getPath();
        User user = usersRepository.findByPhotoContaining(path);
        if (user == null) {
          oldUploads.add(upload);
        }
      } //check uploads
      else if (upload.getPath().contains(uploadsPath)) {
        String path = upload.getPath();
        Post post = postsRepository.findByTextContaining(path);
        PostComment postComment = postCommentsRepository.findByTextContaining(path);
        if (post == null && postComment == null) {
          oldUploads.add(upload);
        }
      }
      //delete non format
      else {
        oldUploads.add(upload);
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