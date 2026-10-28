# Davao Disaster Safety

Davao Disaster Safety (DDS) is a Java desktop application designed to help track families affected by disasters, relief supplies, and relief distributions in Davao. Built using Java Swing and MySQL, the application runs through Apache NetBeans.

## Team Members and Contributions

| Team Member | Role and Contribution |
| --- | --- |
| CYRUS GABRIEL C. MACROHON | **Frontend and Backend Developer** — Develops the Java Swing interface, application logic, navigation, and system functionality. |
| KIERBY M. ANTONIO | **Project Planner and Proposal Developer** — Participates in brainstorming, identifies system requirements, proposes features, and helps prepare the project proposal. |
| DAVEY JAMES A. KHEE | **System Analyst and Requirements Planner** — Collaborates on brainstorming, analyzes the problems the system aims to solve, plans system features, and helps define project requirements. |
| JANJOE CARL C. VILLAQUER | **UI/UX Prototype Support, and System Flow Designer** — Designs the interface layout, creates the system prototype, and develops system flowcharts to illustrate application workflows, navigation, and processes. |


## Features

- **Family Records:** Records families and their barangay details.
- **Relief Inventory:** Tracks relief supplies and stock levels.
- **Relief Distribution:** Records relief distributions and displays distribution history.
- **Map Visualization:** Shows approximate family locations on a map.
- **Member Access:** Allows members to view their family's information and assistance history.

### Demo Data and Limitations

The included records are fictional demonstration data. Map points are approximate and do not represent verified household locations. The application does not provide live disaster alerts or GPS tracking.

## Prerequisites

Install the following software before setting up the project:

- **Java Development Kit (JDK):** Version 17 or newer, compatible with your Apache NetBeans installation.
- **Apache NetBeans IDE:** With Java and Maven project support.
- **MySQL Server:** Version 8.0.16 or newer.
- **MySQL Workbench:** For importing and managing the database.
- **Internet connection:** Required for the initial Maven dependency download and online map tiles.

### Dependencies

The project uses Maven to manage its dependencies.

- **MySQL Connector/J:** Connects the Java application to the MySQL database.
- **JUnit:** Supports automated testing.

These dependencies are declared in `pom.xml` and are downloaded automatically by Maven.

## Installation and Setup

### 1. Download the Repository

1. Open the project's GitHub repository.
2. Click **Code > Download ZIP**.
3. Extract the ZIP file to your preferred folder.

Alternatively, clone the repository using Git:

```bash
git clone <YOUR_PUBLIC_REPOSITORY_URL>
```

Replace the placeholder with your actual GitHub repository URL.

### 2. Set Up the MySQL Database

1. Start MySQL Server.
2. Open MySQL Workbench and connect to your local MySQL server.
3. Select **File > Open SQL Script**.
4. Open `sql/ddsdatabase.sql` from the project folder.
5. Execute the entire SQL script.
6. Refresh the Schemas panel and confirm that the `ddsdatabase` schema appears.

The SQL script creates the database, its nine tables, and fictional sample records. No manual table creation is required.

### 3. Configure the Database Connection

1. Open the project's `config` folder.
2. Copy `database.properties.example`.
3. Rename the copy to `database.properties`.
4. Open `database.properties` in a text editor.
5. Set `db.user` to your MySQL username.
6. Set `db.password` to your MySQL password.
7. Check `db.url` and confirm that the host and port match your MySQL configuration. The default is `localhost:3306`.
8. Save the file.

Keep your actual database credentials private. The real `database.properties` file should be excluded from Git and should never be committed to the public repository.

**Note:** Your MySQL database credentials are separate from the application's demo login credentials.

### 4. Open the Project in Apache NetBeans

1. Launch Apache NetBeans.
2. Select **File > Open Project**.
3. Choose the extracted project folder containing `pom.xml`.
4. Wait for Maven to resolve and download the dependencies.
5. Right-click the project and select **Clean and Build**.
6. Confirm that the build completes successfully.

### 5. Run the Application

1. Ensure that MySQL Server is running.
2. Verify that the database and connection settings are correct.
3. In NetBeans, right-click the project.
4. Select **Run**.
5. Wait for the application window to open.
6. Sign in using one of the demonstration accounts below.

### 6. Demonstration Login Accounts

| Role | Username | Password |
| --- | --- | --- |
| Administrator | `admin` | `Admin@DDS2026` |
| Member | `member` | `Member@DDS2026` |

These accounts are provided for classroom demonstration purposes only. Do not reuse these credentials for real-world deployments.

## Troubleshooting

| Problem | Possible Solution |
| --- | --- |
| Cannot connect to MySQL | Confirm that MySQL Server is running and verify the username, password, hostname, and port in `config/database.properties`. |
| Database or tables are missing | Execute the entire `sql/ddsdatabase.sql` script in MySQL Workbench. |
| Maven build fails | Verify the configured JDK, internet connection, and Maven dependency resolution. Then run **Clean and Build** again. |
| Map does not load | Check your internet connection. Map tiles are retrieved from OpenStreetMap. |
| Application does not start | Check the NetBeans output for error messages and verify the project configuration. |

## Project Structure

| File or Folder | Description |
| --- | --- |
| `src/main/java/` | Java screens, application logic, and database connection code. |
| `src/main/resources/` | Application resources. |
| `src/test/java/` | Automated tests. |
| `sql/ddsdatabase.sql` | Database schema and fictional demo records. |
| `config/database.properties.example` | Example database connection settings. |
| `pom.xml` | Maven project configuration and dependencies. |
| `nbactions.xml` | NetBeans run and debug configuration. |
| `docs/screenshots/` | Project screenshots. |


## Project Status

Davao Disaster Safety is an academic project intended for demonstration and educational purposes. The included data is fictional, and the application should not be relied upon as an operational disaster-response or emergency-alert system.
