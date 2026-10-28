# DDS separate-MySQL edition

## Connection and setup
MySQL Server is installed and started separately. Import sql/ddsdatabase.sql in MySQL Workbench; it creates ddsdatabase, nine InnoDB tables and fictional seed records. The app reads config/database.properties through DBConnection.java and connects with JDBC. It validates imported tables instead of starting a server or auto-seeding. Default URL: jdbc:mysql://localhost:3306/ddsdatabase. Database credentials belong to the local user and are not the application login credentials.

The local URL uses allowPublicKeyRetrieval and unencrypted localhost TCP. Use verified TLS and a restricted application account for a remote deployment. The account used for SQL import needs CREATE DATABASE/table permissions. The runtime app needs SELECT, INSERT and UPDATE on ddsdatabase, and DELETE is not used by the UI. Do not distribute your real password in config/database.properties.

## Code and data design
Main -> Database.open -> DBConnection -> separately running MySQL. SystemInterface contains Swing screens. ReliefService handles validation, prepared SQL, database-backed authorization and transactions. Family/ReliefItem/Distribution/User/ResponseRound are immutable model records. MapPanel draws online OpenStreetMap tiles using Java networking and Graphics2D, with visible attribution, application-specific User-Agent and seven-day viewport-tile caching. Map cache may be created in runtime/map-cache; this is not a database or database server.

Tables: families, users, disasters, response_rounds, relief_items, distributions, stock_movements, audit_logs and schema_info. Distributions reference family/item/round/staff IDs with foreign keys. A UNIQUE key on family_id/item_id/round_id prevents duplicate distributions for the same combination. The event has two seeded rounds; inventory is shared across rounds.

Recording a distribution checks the admin's role, begins a transaction, locks stock with SELECT FOR UPDATE, checks the latest balance, inserts the distribution, deducts stock, adds a stock movement and audit log, then commits. Any failure rolls back the complete operation. Positive-quantity and nonnegative-stock CHECK constraints add database protection. The supplied SQL requires MySQL 8.0.16+ so CHECK constraints are enforced.

Passwords are stored as salted PBKDF2WithHmacSHA256 hashes (180000 iterations), not plaintext. Member queries obtain the permitted family ID from MySQL. The table viewer allowlists identifiers and excludes password hashes. CSV export quotes fields and guards leading spreadsheet-formula characters. Distribution history is read-only; no reversal/edit/delete workflow is included.

"Assisted" means at least one item was received in the selected round; it does not mean all needs were met. Registration does not create a login account. Account/event/round management, password reset, backup/restore UI, distribution reversal and verified live hazard feeds are future work. The included dataset and map points are fictional and approximate. This is a classroom app, not a deployed disaster operations service.

## Verification
The included MySQL integration tests require DDS_TEST_PASSWORD and optionally DDS_TEST_PORT. They create and remove isolated temporary test databases. The Swing smoke test requires -Ddds.uiTest=true and a configured demo database. These tests are opt-in; a successful build alone does not verify database connectivity or the screens.

## References
- MySQL: https://dev.mysql.com/downloads/mysql/8.4.html
- Connector/J: https://dev.mysql.com/doc/connector-j/en/
- OSM attribution: https://www.openstreetmap.org/copyright
- OSM tile policy: https://operations.osmfoundation.org/policies/tiles/
- PSA Davao barangays: https://psa.gov.ph/classification/psgc/barangays/1130700000
- Figma: https://www.figma.com/design/T6CR7iZfacjTvXfULvl1jV/

This edition replaces the earlier bundled-server setup. The database now resides on the separately installed MySQL server, outside the Java project.
