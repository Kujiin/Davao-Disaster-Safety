# Davao Disaster Safety

A Java desktop app for keeping track of families, relief supplies, and relief distributions in Davao. Built with Java Swing and MySQL, and opened through Apache NetBeans.

## Team

| Team member | Contribution |
| --- | --- |
| [Your full name] (Kujiin) | Frontend and backend development |
| [Groupmate name] | [Actual contribution] |
| [Groupmate name] | [Actual contribution] |

Replace the placeholders with the correct names and contributions before submission. Add or remove rows to match the team.

## What the app does

- Records families and their barangay details.
- Tracks relief supplies and stock levels.
- Records relief distributions and shows distribution history.
- Shows approximate family locations on a map.
- Lets members view their own family's information and assistance history.

The included records are fictional demo data. Map points are approximate; the app does not provide live disaster alerts or GPS tracking.

## What you need

- Java JDK 17 or newer that your NetBeans version supports.
- Apache NetBeans with Java and Maven support.
- MySQL Server 8.0.16 or newer (MySQL 8.4 is suitable).
- MySQL Workbench to import the database.
- Internet for the first build to download dependencies, and for map tiles.

MySQL Connector/J and JUnit are listed in `pom.xml`. Maven downloads them automatically.

## How to set it up

### 1. Download the project

Click **Code > Download ZIP** on GitHub, then extract the ZIP. You can also clone the repository.

### 2. Create the database

1. Start MySQL Server.
2. Open MySQL Workbench and connect to your local server.
3. Choose **File > Open SQL Script** and open `sql/ddsdatabase.sql` from this project.
4. Run the entire script using **Execute All**.
5. Refresh the Schemas list. You should see **ddsdatabase**.

This script creates the database, its nine tables, and fictional sample records. You do not need to create the tables by hand.

### 3. Enter your database settings

1. Open the project's `config` folder.
2. Copy `database.properties.example` and name the copy `database.properties`.
3. Open the copy in a text editor.
4. Set `db.user` to your MySQL username and `db.password` to your MySQL password. Leave the password blank only if your MySQL account has no password.
5. The default connection uses `localhost:3306`. Change the host or port in `db.url` if your server uses something else.
6. Save the file. Keep your real settings on your computer; this file is excluded from Git.

Your MySQL password is separate from the app login below.

### 4. Open and run in NetBeans

1. Choose **File > Open Project**.
2. Select the extracted project folder containing `pom.xml`.
3. Wait for NetBeans to download the dependencies.
4. Right-click the project and choose **Clean and Build**.
5. Right-click the project and choose **Run**.

Keep MySQL Server running while using the app.

### 5. Sign in with a demo account

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `Admin@DDS2026` |
| Member | `member` | `Member@DDS2026` |

These accounts are supplied only for the classroom demo.

## If something does not work

- **Cannot connect to MySQL:** check that the server is running and that the username, password, and port in `config/database.properties` are correct.
- **Missing database or tables:** run the entire `sql/ddsdatabase.sql` script before starting the app.
- **Build fails:** check the selected JDK and your internet connection, then try Clean and Build again.
- **Map does not load:** check your internet connection. Map tiles come from OpenStreetMap.

## Main files

| File or folder | Purpose |
| --- | --- |
| `src/main/java/` | Java screens, app logic, and database connection code |
| `src/main/resources/` | Resources used by the project |
| `src/test/java/` | Existing automated tests |
| `sql/ddsdatabase.sql` | Database setup and demo records |
| `config/database.properties.example` | Example database settings |
| `pom.xml` | Build settings and dependencies |
| `nbactions.xml` | NetBeans run and debug settings |
| `docs/screenshots/` | Screenshots from the earlier project package |

Generated builds, runtime files, and real database settings are excluded from this repository.
