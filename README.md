# ☕🍃

## Online Book Store Application📚

---

## 📝Content
* [Overview](#overview)
* [Technologies](#technologies)
* [Features](#features)
* [Project Structure](#project-structure)
* [Run Project](#run-project)

### 🗺️Overview
The Online Book Store Application is a web-based system that allows users 
to browse, search, and purchase books online. It is built using 
the following technologies:

* ### 👨‍💻Technologies
   * **Java**: The core programming language used for development.
   * **Spring Boot**: A Java framework used to build the application.
   * **Maven**: Apache Maven is a software project management and comprehension tool.
   * **Mockito**: A testing framework used for unit testing.
   * **MapStruct**: A Java-based code generation tool used for mapping between objects.
   * **Docker**: Used for containerization and deployment of the application.
   * **JWT** *(JSON Web Tokens)*: Used for authentication and security.
   * **MySQL**: A relational database management system used to store book and user information.
   * **Lombok**: A library used to reduce boilerplate code.
   * **Liquibase**: A database migration tool used for managing database schema changes.

### 👀Features
* User Registration and Authentication.
* Browse and Search Books.
* View Book Details.
* Add Books to Cart.
* View Cart Items.
* View Order History.

###  🗃️Project Structure
[com.bookstore](src/main/java/com/bookstore)
* [config](src/main/java/com/bookstore/config) : config classes for mappers and security 
* [controller](src/main/java/com/bookstore/controller) : http controllers
* [dto](src/main/java/com/bookstore/dto) : data transfer objects for http responses and requests
* [exception](src/main/java/com/bookstore/exception) : custom exceptions and [CustomGlobalExceptionHandler.java][1] 
for handling exceptions
* [mapper](src/main/java/com/bookstore/mapper) : mappers for converting entities into DTOs and vice versa
* [model](src/main/java/com/bookstore/model) : entity classes used in app
* [repository](src/main/java/com/bookstore/repository) : repositories for CRUD operations with database
* [security](src/main/java/com/bookstore/security) : classes to implement security into project 
* [service](src/main/java/com/bookstore/service) : classes for business logic
* [validation](src/main/java/com/bookstore/validation) : custom validation annotations
* [Main class](src/main/java/com/bookstore/BookStoreApplication.java) : class to run application

[resources](src/main/resources) 
* [db.changelog](src/main/resources/db/changelog) : directory with liquibase scripts for managing database
   * [changes](src/main/resources/db/changelog/changes) : liquibase scripts
   * [changelog-master](src/main/resources/db/changelog/db.changelog-master.yaml) : file to run scripts
* [application.properties](src/main/resources/application.properties) : application configurations

[test](src/test)
* [application test](src/test/java/com/bookstore) : classes for testing app
* [test/resources](src/test/resources) : test resources
   * [database](src/test/resources/database) : sql scripts used in testing
   * [test/application.properties](src/test/resources/application.properties) : test application configurations

---

#### root directory files:
* [.env](.env) : contains credentials for docker database connection
* [checkstyle.xml](checkstyle.xml) : checkstyle rules file
* [docker-compose.yml](docker-compose.yml) : with a single command, 
you create and start all the services from your configuration thanks to this file
* [dockerfile](Dockerfile) : docker configurations
* [pom.xml](pom.xml) : maven configurations

---

###  ✅Run project
1. Clone the repository🔗:
`git@github.com:fmIst0/online-book-store.git`
2. Build project with MavenⓂ️ : `mvn clean intall`
3. Ensure you have Docker🐳 installed on your system
4. Open terminal and run the application using 
Docker Compose with command : `docker-compose up`
5. Use Postman and Swagger to explore the application🥳
6. In terminal write `docker-compose down` to stop containers

## Or try to test with [Swagger](http://ec2-16-170-234-204.eu-north-1.compute.amazonaws.com/swagger-ui/index.html#/)📍

### Login
{<br/>
"email": "awsadmin@example.com",<br/>
"password": "1234567890"<br/>
}





[1]: src/main/java/com/bookstore/exception/CustomGlobalExceptionHandler.java  