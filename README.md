# Lost and Found Management System

## Overview

The Lost and Found Management System is a full-stack application designed to facilitate the reporting, tracking, and claiming of lost and found items. It provides a secure, organized platform connecting individuals who have lost belongings with those who have found them, supported by role-based access control and a formal claim verification process.

## Technology Stack

**Backend** :
- Java 17
- Spring Boot 3.2.0 (Spring Web, Spring Data JPA, Spring Security)
- Hibernate (ORM)
- Maven (Dependency Management)
- Lombok (Boilerplate Reduction)

**Database** : 
- MySQL 8.0 (Relational Database)

**Frontend** : 
- Streamlit (Python-based interactive web UI)

## Key Features

- **User Authentication & Role Management**: Secure registration and login functionality. The system enforces Role-Based Access Control (RBAC), distinguishing between standard USER accounts and ADMIN accounts.
- **Item Reporting**: Users can submit detailed reports for both lost and found items. Reports include descriptions, dates, and image references.
- **Categorization & Location Tracking**: Items are strictly associated with defined Categories (e.g., Electronics, Documents) and Locations (e.g., Library, Cafeteria) to optimize search and filtering capabilities.
- **Formal Claim System**: Users browsing found items can initiate a formal Claim, requiring them to submit proof of ownership for review.

## Database Architecture

The system utilizes a robust object-oriented database schema design:

- **Single Table Inheritance**: The users table stores both regular Users and Admins, separated by a discriminator column. The items table stores both LostItem and FoundItem entities, sharing common attributes while maintaining their unique data fields through discriminator mapping.
- **Relational Mapping**: Complex bidirectional relationships map Users to their reported Items, Claims, and Notifications.

## Software Engineering Principles Applied

The system architecture rigorously adheres to established software design methodologies to ensure maintainability, scalability, and robust code structure.

## Object-Oriented Analysis and Design (OOAD)

- **Encapsulation**: Class properties are restricted using access modifiers, with strictly controlled data manipulation facilitated by generated getters, setters, and validation annotations.
- **Abstraction**: Core domain concepts are generalized into abstract base classes (e.g., Item), preventing the instantiation of undefined generic entities.
- **Inheritance**: Extensively utilized for entity mapping, allowing specific models (Admin, LostItem, FoundItem) to inherit core attributes from base classes (User, Item) while extending their own distinct properties.
- **Polymorphism**: Implemented through method overriding and interface execution, allowing the system to seamlessly process collections of varying entity subtypes using base-class references.

## SOLID Principles
- **Single Responsibility Principle (SRP)**: Each class serves a single, well-defined purpose. Domain models solely represent data, while repositories handle data access.
- **Open/Closed Principle (OCP)**: The underlying domain models are designed to be extensible. New item variants or user roles can be introduced by extending base classes without modifying existing logic.
- **Liskov Substitution Principle (LSP)**: Subclasses inherently fulfill the contracts of their base classes, ensuring that substituting an Item reference with a LostItem instance maintains system stability.
- **Interface Segregation Principle (ISP)**: Client-specific interfaces are leveraged via Spring Data JPA, ensuring modules only depend on the precise repository methods they require.
- **Dependency Inversion Principle (DIP)**: System components rely on abstractions rather than concrete implementations, facilitated heavily by Spring Boot's Inversion of Control (IoC) and dependency injection.

## GRASP Principles
- **Controller**: User interface events are decoupled from internal business logic and routed through designated REST controllers, effectively managing the flow of data.
- **Information Expert**: Methods and behaviors are assigned directly to the classes containing the data required to fulfill the responsibility.
- **Low Coupling & High Cohesion**: Architectural layers (Models, Repositories, Controllers) remain independent and focused on specific domains, minimizing the ripple effect of code changes.
- **Creator**: The responsibility of instantiating dependent objects (such as mapping a Claim to a specific Item and User) is assigned to the service layers possessing the required initialization data.

## Local Setup and Installation

### Prerequisites

- Java Development Kit (JDK) 17
- Maven
- MySQL Server (running locally on port 3306)
- Python 3.x

**1. Database Configuration** : 
Open your MySQL terminal or client and execute the following command to create the required database:

```
CREATE DATABASE lostandfound_db;
```

**2. Backend Environment Setup** :
   
Navigate to src/main/resources/application.properties in your Spring Boot project and verify your database credentials:

```
spring.datasource.url=jdbc:mysql://localhost:3306/lostandfound_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=Your_MySQL_Password
```

JPA Configuration

```
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

**3. Running the Backend Server** :
Open a terminal in the root directory of your Java project and execute the Maven run command. The server will initialize on http://localhost:8080.

```
mvn spring-boot:run
```

**4. Running the Frontend Client** :
Open a separate terminal instance, navigate to your frontend directory, install the required Python packages, and launch the Streamlit application:

```
pip install streamlit requests
streamlit run app.py
```

(Ensure you replace app.py with the specific filename of your frontend script if different).

### Future Roadmap

- Implementation of an automated matching algorithm to correlate lost reports with found reports based on metadata.
- Integration of external notification gateways (Email/SMS).
- Implementation of a geospatial mapping interface for location tracking.
