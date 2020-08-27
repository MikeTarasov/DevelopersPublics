package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.repository.CaptchaCodesRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.List;


@Service
public class CaptchaCodeService {

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;

    public List<CaptchaCode> getAllCaptchaCodes() {
        List<CaptchaCode> captchaCodes = new ArrayList<>();
        for (CaptchaCode captchaCodeDB : captchaCodesRepository.findAll()) {
            captchaCodes.add(captchaCodeDB);
        }
        return captchaCodes;
    }

    public void saveCaptcha(String code) {
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(code);
        captchaCode.setTime(Instant.now().toEpochMilli());
        captchaCode.setSecretCode(code);
        captchaCodesRepository.save(captchaCode);
    }

    public void deleteOldCaptcha(long captchaLifeTime) {
        long timestampLifeTime = Instant.now().toEpochMilli() - captchaLifeTime;
        ArrayList<CaptchaCode> oldCaptchaList = new ArrayList<>();
        for (CaptchaCode captchaCode : captchaCodesRepository.findAll()) {
            if (captchaCode.getTimestamp() < timestampLifeTime) {
                oldCaptchaList.add(captchaCode);
            }
        }
        captchaCodesRepository.deleteAll(oldCaptchaList);
    }

    @SneakyThrows
    public JSONObject createNewCaptcha() {
        JSONObject result = new JSONObject();
        //кол-во символов
        int iTotalChars = 6;
        //высота капчи
        int iHeight = 35;
        //ширина капчи
        int iWidth = 100;
        //шрифт
        int fontSize = (int) (1.67 * iWidth / iTotalChars);
        //фон
        Font fntStyle = new Font("Arial", Font.BOLD, fontSize);

        Random randChars = new Random();
        //генерируем слово
        String code = (Long.toString(Math.abs(randChars.nextLong()), 36)).substring(0, iTotalChars);
        //генерируем картинку
        BufferedImage biImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dImage = (Graphics2D) biImage.getGraphics();
        int iCircle = 15;
        for (int i = 0; i < iCircle; i++) {
            g2dImage.setColor(new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
        }
        g2dImage.setFont(fntStyle);
        for (int i = 0; i < iTotalChars; i++) {
            g2dImage.setColor(new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
            if (i % 2 == 0) {
                g2dImage.drawString(code.substring(i, i + 1), (int)(fontSize * i *0.6), (int)(fontSize/1.25));
            } else {
                g2dImage.drawString(code.substring(i, i + 1), (int)(fontSize * i * 0.6), (int)(iHeight-fontSize/4));
            }
        }
        //создаем временный файл в нужном формате
        File file = new File("target/" + code + ".png");
        ImageIO.write(biImage, "png", file);
        //кодируем картинку в текст
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        //убираем мусор
        file.delete();
        g2dImage.dispose();

        //сохраняем капчу в репозиторий
        saveCaptcha(code);

        result.put("code", code);
        result.put("base64", base64);

        return result;
    }
}
