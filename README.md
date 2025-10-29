# Project "Cloud File Storage"

A multi-user cloud file storage. Users can upload and store files using this service. The project is built as a REST API.

## Technologies / Tools Used

### Backend
Java 17  
Spring Boot  
Spring Security  
Spring Sessions  
Redis  
Lombok  
Mapstruct  
Maven  
Swagger  
Docker  

### Database
Spring Data JPA  
PostgreSQL  
Minio  
Flyway  

### Testing
JUnit 5  
AssertJ  
Testcontainers  

Running the Project
1. Clone the repository using the command:  
```git clone https://github.com/Dimas-Ukimas/cloud-storage.git```
3. Open the cloned repository folder in IntelliJ IDEA.
4. Create a .env file in the root of the project and fill it according to the .env.example template.
5. In the terminal, navigate to the root of the project and run:  
```docker compose up -d```
7. The backend will be available at:
http://localhost  
Swagger UI documentation can be accessed at:
http://localhost:8080/api/swagger-ui/index.html
