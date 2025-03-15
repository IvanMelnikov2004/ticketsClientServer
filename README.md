# ticketsClientServier
для Траектория Будущего
# Инструкция по сборке и запуску проекта

## Требования
- Java 17+
- Apache Maven 3.6+
- PostgreSQL 13+
- Веб-браузер (Chrome, Firefox или аналоги)
- Любой веб-сервер для статических файлов (Python, Live Server и т.д.)

## 1. Настройка базы данных

1. Установите PostgreSQL и запустите сервер
2. Создайте базу данных:
```bash
createdb ticket_booking
Создайте пользователя БД:

sql
Copy
CREATE USER booking_user WITH PASSWORD 'your_strong_password';
GRANT ALL PRIVILEGES ON DATABASE ticket_booking TO booking_user;
2. Запуск серверной части (Spring Boot)
Перейдите в директорию проекта:

bash
Copy
cd path/to/spring-project
Настройте подключение к БД в src/main/resources/application.properties:

properties
Copy
spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_booking
spring.datasource.username=booking_user
spring.datasource.password=your_strong_password
spring.jpa.hibernate.ddl-auto=update
Соберите проект:

bash
Copy
mvn clean package
Запустите приложение:

bash
Copy
java -jar target/your-app-name.jar
3. Запуск клиентской части
Поместите файлы фронтенда (HTML, CSS, JS) в отдельную директорию

Настройте базовый URL API в клиентском коде:

javascript
Copy
const apiBaseUrl = "http://localhost:8080";
Запустите веб-сервер (выберите один вариант):

Вариант 1: Python
bash
Copy
python -m http.server 8000
Вариант 2: VS Code Live Server
Установите расширение "Live Server"

Откройте index.html

Нажмите "Go Live" в статус-баре

4. Настройка CORS
Добавьте в Spring-приложение конфигурацию:

java
Copy
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
5. Проверка работоспособности
Откройте в браузере: http://localhost:8000
