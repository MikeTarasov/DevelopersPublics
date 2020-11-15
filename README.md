# Developers Publications
Для запуска приложения необходимо:
1. *База Данных: mySQL8 или Postgre 11/12 (работающий сервер с пустой БД),
2. *Зарегистрированный пользователь в gmail.

Заполняем системные переменные в application.yml:
1. Данные для подключения к БД: 

    *db.type, 

    *db.port,

    *db.host,

    *db.name,

    *db.timezone,

    *spring.dataSource.username,

    *spring.dataSource.password .

2. Адрес блога: 

    *blog.host .

3. Данные gmail почты: 

    *moderator.email,

    *moderator.email.password .

4. Директорию на сервере для изображений: 

    *uploads.home .

После этого создаем .jar файл и запускаем его на сервере.

Порт, по которому обращаться - 8080.

Путь к приложению - http://mktarasov-java-pg-skillbox.herokuapp.com
