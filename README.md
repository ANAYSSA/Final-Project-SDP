# Hearth & Table

A Restaurant Ordering System built with **Java 21, Spring Boot 3.5.16, HTML, CSS and vanilla JavaScript**. Six design patterns cooperate in a complete ordering flow: choose a meal, add toppings, assemble an order, apply a discount, render a receipt and notify subscribers when its status changes.

This is a coursework demonstration. It does not charge money or send orders to a real restaurant. Orders belong to the current browser session and are stored in memory. Restarting the server or expiring the session clears them.

**Live demo:** https://hearth-table.178.62.244.88.sslip.io

## Run locally

Install JDK 21 and set `JAVA_HOME` to its installation directory. Maven is downloaded automatically by the wrapper on first use.

```sh
./mvnw verify
java -jar target/hearth-and-table-1.0.0.jar
```

Open http://localhost:8080. On Windows use `mvnw.cmd verify`.

Run the six-pattern console demonstration without starting a web server:

```sh
java -jar target/hearth-and-table-1.0.0.jar --demo
```

Check the coursework package structure:

```sh
python3 scripts/check_structure.py
```

## Pattern ownership

| Member | Patterns | Category |
| --- | --- | --- |
| Anuar | Builder, Factory Method | Creational |
| Akmaral | Strategy, Bridge | Behavioral, structural |
| Mukhammad | Observer, Decorator | Behavioral, structural |

This table records the agreed responsibilities. The initial integrated baseline is committed under Anuar's authenticated Git identity. It is **not evidence that every member has individually completed their two patterns**. Akmaral and Mukhammad must implement, test and commit their own contributions before submission. See [team tasks and agent prompts](docs/TEAM_TASKS.md).

## Structure

```text
src/main/java/com/restaurant/
├── Main.java
├── builder/       # immutable order, application orchestration, HTTP boundary
├── factory/       # abstract creator, concrete creators, meal products, catalog
├── strategy/      # discount interface, concrete algorithms, pricing context
├── bridge/        # receipt abstractions and independent rendering implementations
├── observer/      # subject, observers, status events
└── decorator/     # wrapping component, concrete toppings, catalog
```

Pattern roles live in explicit subfolders. `Main` is the only Java source directly in `com.restaurant`. Web adapters are inside `builder.web` to preserve the required six-package hierarchy; they orchestrate the domain but do not replace its pattern roles. Test classes follow the same pattern package boundaries.

## Documentation

- [English PDF report: 15 pages, Java excerpts and screenshots](docs/REPORT.pdf)
- [UML: six class diagrams and an integrated sequence](docs/uml/README.md)
- [Verification evidence](docs/VALIDATION.md)
- [Requirements and submission checklist](docs/REQUIREMENTS.md)
- [Team contributions and copy/paste agent prompts](docs/TEAM_TASKS.md)
- [Defense guide: OOP, SOLID and patterns](docs/DEFENSE_GUIDE.md)
- [Deployment and maintenance](docs/DEPLOYMENT.md)

## Continuity with earlier assignments

The project continues Anuar's pizzeria exercises: [Builder](https://github.com/ANAYSSA/SDPAssignment1Builder), [Abstract Factory](https://github.com/ANAYSSA/Assignment2SDP_AbstractFactory_AnuarM), [Bridge](https://github.com/ANAYSSA/sdp-assignment-3), [Facade](https://github.com/ANAYSSA/as4-facade-pizzeria), [Strategy](https://github.com/ANAYSSA/as5-strategy-pizzeria), and [Visitor](https://github.com/ANAYSSA/as6-visitor-pizzeria). Prices are in KZT; familiar meals and student/pickup discounts remain. This final uses Factory Method explicitly; it does not count Facade or Visitor toward its six selected patterns.

## Regenerate the report

After rerunning tests and updating screenshots, install optional Python `reportlab` and run `python3 scripts/build_report.py`. The generator reads current Java excerpts, Surefire results, UML figures and captured screenshots. See `docs/uml/README.md` to regenerate UML. Refresh the factual contribution statement after merging teammate work.
