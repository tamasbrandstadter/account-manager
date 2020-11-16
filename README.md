# Account Manager API

Account Manager was created for managing financial accounts for payment solutions. REST API endpoints are exposed
for:
- Opening accounts (creating new accounts)
- Depositing amount to account
- Withdrawing amount from account
- Transferring amount between accounts
- Requesting the current balance of account.

## Technology stack
* The project uses `OpenJDK 15`, therefore you have to install it first on your local machine if you want to develop the API.

* The API is implemented via `Spring Boot` (current version is `2.4.0`) in reactive manner for request handling, 
data processing and database connectivity.
For more information visit:
    - [Reactive Spring](https://spring.io/reactive)
    - [R2dbc](https://r2dbc.io/)

* `Postgres` is used for persisting data.

* The project build tool is `Gradle`. 

## Running locally
* If you want to run the application locally you have to install `Docker`.

* Once you have it, execute the following commands in the project root folder:
    - `./gradlew clean && ./gradlew build`
    - `docker build -t accountmanager:latest .`
    - `docker-compose up`

* Docker will start the containers (both the database and the API) and you can start using them. 
The API port is exposed to 8080 of your localhost.

## Testing
* Unit and integration tests are implemented as part of the project.

* Tests are using `H2` as database.