package main.com.skillbox.ru.developerspublics.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.Uploads;
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
      FileOutputStream fos = new FileOutputStream(pathServer);
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
      String path = upload.getPath();
      //check avatars
      if (path.contains(avatarPath) && usersRepository.findByPhotoContaining(path) == null) {
          oldUploads.add(upload);
      } //else check uploads
      else if (path.contains(uploadsPath) &&
               postsRepository.findByTextContaining(path) == null &&
               postCommentsRepository.findByTextContaining(path) == null) {
          oldUploads.add(upload);
      }
    }

    if (oldUploads.size() != 0) {
      for (Uploads upload : oldUploads) {
        uploadsRepository.delete(upload);
        new File(upload.getPath()).delete();
      }
    }
  }
}