# Requirements and submission checklist

Hearth & Table is a Java Restaurant Ordering System with an HTML, CSS and JavaScript interface served by Spring Boot. The course requirements come from **Final Project Defense Requirements.pdf** (especially page 1) and the additional requirements supplied by the team. The topic examples on pages 2–4 and reading suggestions on page 5 are supporting material, not additional mandatory features.

This checklist distinguishes software evidence from work that the students must complete themselves. A functioning baseline does not establish that all three students have individually implemented their two patterns.

## Pattern ownership and integration

| Student | Assigned patterns | Categories | Intended behavior |
| --- | --- | --- | --- |
| Anuar | Builder; Factory Method | Creational; creational | Construct validated orders; create different menu products through concrete creators |
| Akmaral | Strategy; Bridge | Behavioral; structural | Select a discount algorithm; vary receipt purpose independently from output format |
| Mukhammad | Observer; Decorator | Behavioral; structural | Notify subscribers about order status; compose extra toppings and prices |

The initial integrated baseline is committed under Anuar's configured Git identity. It is **not** evidence of commits or completed individual implementation by Akmaral or Mukhammad. Their substantive implementation, tests, explanations and commits remain required. See [TEAM_TASKS.md](TEAM_TASKS.md) for separate follow-up work. Do not rewrite author fields, backdate commits or create empty commits to simulate contributions.

## Requirement-to-evidence map

| Source | Requirement | Evidence to inspect / completion action |
| --- | --- | --- |
| PDF, page 1 | Team project, preferably 2–3 students | Three named team members above |
| PDF, page 1 | Every student individually implements at least two patterns | Inspect each person's actual changes and original Git author history; teammate work is still pending after the baseline |
| PDF, page 1 | At least six distinct patterns | Builder, Factory Method, Strategy, Bridge, Observer and Decorator; inspect the six matching packages |
| PDF, page 1 | Patterns operate within one cohesive application | Run the full ordering flow: product creation → toppings → order construction → discount → status notifications → receipt rendering |
| PDF, page 1 | All patterns demonstrably operational | Run `Main --demo`, the automated tests and the browser demonstration described in [DEFENSE_GUIDE.md](DEFENSE_GUIDE.md) |
| Additional | At least one creational, structural and behavioral pattern | Two patterns in each category, as listed above |
| Additional | Clean Code, SOLID, KISS and DRY | Explain specific classes and extension points; use the examples in the defense guide instead of unsupported compliance claims |
| Additional | Correct package hierarchy; six pattern packages; only `Main` outside | Check production Java sources: `com.restaurant.Main` is the only class at the package root; other classes belong under the six pattern packages and role subpackages |
| Additional | Git repository with all members' commit histories | Public repository: https://github.com/ANAYSSA/Final-Project-SDP ; inspect authors and substantive changes after merging teammates' branches |
| PDF, page 1 | Report includes introduction, body, conclusion and further work | Check the final report's contents before submission |
| PDF, page 1 | Report includes Java/Go code, application screenshots and UML diagrams | Include Java excerpts tied to the real classes, screenshots of the running application and readable class/interaction diagrams |
| PDF, page 1 | PDF report and zipped source code uploaded to Moodle | Each student uploads the finished PDF and source ZIP; a GitHub link alone does not replace the ZIP |
| Additional | Every student submits on time; no late submissions | Each student verifies their own Moodle submission and the course's published deadline |
| PDF, page 1; grading criteria | Oral defense before teaching staff; OOP and patterns questions | Each student rehearses their two assigned patterns and the complete application flow |

The material supplied with this project does **not** state an actual deadline date or time. Use the deadline shown in Moodle or provided by the lecturer; do not infer it from repository timestamps.

## Package rule

The production source root is `src/main/java/com/restaurant/`:

```text
com/restaurant/
├── Main.java
├── builder/     # Builder roles and order application/web infrastructure
├── factory/     # Product and creator hierarchies
├── strategy/    # Discount contract, strategies and context
├── bridge/      # Receipt abstractions and renderer implementations
├── observer/    # Subject, observer contract and subscribers
└── decorator/   # Component decorators for toppings
```

Infrastructure is located in named subpackages beneath `builder` to keep the requested six-package boundary. It should remain separate from Builder role classes. This is a course-specific packaging choice; the web controller is not itself a Builder participant. HTML/CSS/JavaScript resources are not Java classes. Tests belong in the matching pattern package trees under `src/test/java`, and must not introduce an additional production package outside the six.

## Final verification

Use the exact commands documented in the repository README. Before submission:

1. Run the Maven verification suite and the integrated `--demo` mode on Java 21.
2. Demonstrate the live menu, topping selection, discount calculation, order creation, status transitions and both receipt purposes/formats.
3. Check the Java package tree and the UML against the actual code after teammates' changes.
4. Inspect `git shortlog -sne --all` and the relevant commits. Counts alone do not prove meaningful contributions; review changed code and tests.
5. Merge reviewed teammate work while preserving its original authors. Confirm that the final source ZIP corresponds to the submitted repository revision.
6. Refresh the report's screenshots, code excerpts, contribution statement and test results against that final revision.
7. Each student submits the PDF, source ZIP and repository link in Moodle and verifies successful upload before the course deadline.

Deployment is requested by the team for demonstration. It is additional to the PDF's submission requirements. Demo orders and payment simulation must be described accurately; the application does not process real payments.

## Grading stated by the course

| Component | Weight |
| --- | ---: |
| Submitted project code and Git link | 50% |
| Submitted project report | 10% |
| Defense: OOP and design patterns | 40% |

The additional instructions state a **5% deduction for any violation**, including violations of the core PDF requirements. This document records the supplied rule; it does not predict the lecturer's final assessment.
