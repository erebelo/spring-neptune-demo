# Spring Neptune Demo

REST API project developed in Java using Spring Boot 3, Neptune and TinkerGraph database.

## Requirements

- Java 17
- Spring Boot 3.x.x
- Apache Maven 3.8.6

## Libraries

- [spring-common-parent](https://github.com/erebelo/spring-common-parent): Manages the Spring Boot version and provide common configurations for plugins and formatting.

## Configuring Maven for GitHub Dependencies

To pull the `spring-common-parent` dependency, follow these steps:

1. **Generate a Personal Access Token**:

   Go to your GitHub account -> **Settings** -> **Developer settings** -> **Personal access tokens** -> **Tokens (classic)** -> **Generate new token (classic)**:

    - Fill out the **Note** field: `Pull packages`.
    - Set the scope:
        - `read:packages` (to download packages)
    - Click **Generate token**.

2. **Set Up Maven Authentication**:

   In your local Maven `settings.xml`, define the GitHub repository authentication using the following structure:

   ```xml
   <servers>
     <server>
       <id>github-spring-common-parent</id>
       <username>USERNAME</username>
       <password>TOKEN</password>
     </server>
   </servers>
   ```

   **NOTE**: Replace `USERNAME` with your GitHub username and `TOKEN` with the personal access token you just generated.

## Run App

- Set the following environment variables if running the project for a spring profile other than `local`: `AWS_REGION`, `AWS_NEPTUNE_ENDPOINT`, `AWS_NEPTUNE_PARTITION_KEY`, and `AWS_NEPTUNE_PARTITION_NAME`.
- Use the application property `aws.neptune.embedded` to switch between Neptune and TinkerGraph database.
- Run the `SpringNeptuneDemoApplication` class as Java Application.

## DB Preview

- Hit [Graph Data](http://localhost:8080/spring-neptune-demo/graph/data) to view the graph json data
- Hit [Graph Viewer](http://localhost:8080/spring-neptune-demo/index.html) to view the graph vertex/edge representation

## Collection

[Project Collection](https://github.com/erebelo/spring-neptune-demo/tree/main/collection)

## Diagram

[Entity Relationship Diagram](https://github.com/erebelo/spring-neptune-demo/tree/main/db/Entity%20Relationship%20Diagram.png)
