# BASE Jump Logbook

JavaFX desktop application for logging BASE jumps and viewing integrated 5-day weather forecasts.

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Installation](#installation)
5. [Usage](#usage)
   - [Run from Source](#run-from-source)
   - [Download & Run](#download--run)
6. [Building a Fat JAR](#building-a-fat-jar)
7. [Contributing](#contributing)
8. [License](#license)

## Features

- Log jumps with date/time, location, GPS, height, gear details, jump type, media link
- View 5-day temperature and wind speed charts for selected jump location
- Embedded map view (OpenStreetMap) of jump coordinates
- Full Create / Read / Update / Delete (CRUD) interface
- Export jump log to CSV
- Packaged as a single "fat" JAR for easy distribution

## Tech Stack

- Java 17
- JavaFX (Controls, FXML, WebView)
- Hibernate ORM with H2 embedded database
- OpenWeatherMap API for weather data
- Maven build system
- JUnit 5 and Mockito for testing

## Prerequisites

- Java Development Kit 17 or higher
- Maven 3.6+

## Installation

1. Clone the repository  
   ```bash
   git clone https://github.com/masonakcamara/base-jump-logbook.git
   cd base-jump-logbook
   ```  
2. Copy the API key template and insert your OpenWeatherMap key  
   ```bash
   cp src/main/resources/weather.properties.template src/main/resources/weather.properties
   # edit src/main/resources/weather.properties and set api.key=YOUR_API_KEY
   ```

## Usage

### Run from Source

```bash
mvn clean javafx:run
```

### Download & Run

Every push builds and publishes a shaded JAR artifact. To download and run:

1. Go to **Actions → base-jump-logbook → Artifacts** on GitHub  
2. Download the `base-jump-logbook-*.jar` file  

```bash
java -jar base-jump-logbook-1.0-SNAPSHOT-shaded.jar
```

## Building a Fat JAR

From the project root:

```bash
mvn clean package
```

The shaded JAR will be in `target/` named `base-jump-logbook-1.0-SNAPSHOT-shaded.jar`.

## Contributing

Contributions are welcome. Please fork the repo and open a pull request for any bug fixes or enhancements.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
