# Requirements and submission checklist

The requirements come from page 1 of **Final Project Defense Requirements.pdf** and the additional course instructions. The topic examples and suggested reading are supporting material.

## Team work

| Student | Patterns | Implementation evidence |
| --- | --- | --- |
| Anuar | Builder, Factory Method | Integrated application and initial implementations in `954a3c8` (`ANAYSSA`) |
| Akmaral | Strategy, Bridge | `FamilyDiscount` in `1debe3f`; `JsonReceiptRenderer` in `7475264` (`Akma`, GitHub `arynttai`) |
| Mukhammad | Decorator, Observer | `HerbsDecorator` in `f51cc9c`; `OperationsDashboardObserver` in `b2e5b55` (`Vertex`, GitHub `Vertex-blip`) |

[PR #1](https://github.com/ANAYSSA/Final-Project-SDP/pull/1) and [PR #2](https://github.com/ANAYSSA/Final-Project-SDP/pull/2) were reviewed and merged with merge commits `e505627` and `a91e948`. The original student commits remain in the history. Their explanations are in [AKMARAL.md](contributions/AKMARAL.md) and [MUKHAMMAD.md](contributions/MUKHAMMAD.md). Each student still needs to explain their work at the defense.

## Requirement-to-evidence map

| Requirement | Evidence or remaining action |
| --- | --- |
| At least six distinct patterns in one project | Builder, Factory Method, Strategy, Bridge, Observer and Decorator cooperate in the ordering flow. |
| At least one pattern of each category | Two creational, two structural and two behavioral patterns. |
| Each student implements at least two patterns | Pattern assignments, implementation commits and merged PRs are listed above. Review the code changes as well as the author history. |
| All patterns demonstrably operational | Run the test suite, `Main --demo` and the browser scenario in [DEFENSE_GUIDE.md](DEFENSE_GUIDE.md). |
| Clean Code, SOLID, KISS and DRY | Separate creation, pricing, rendering and notification contracts; shared server-side quote/order calculations; explicit validation. |
| Six pattern packages and only `Main` outside them | Run `python3 scripts/check_structure.py`. `Main.java` is the only source directly in `com.restaurant`. |
| Git repository and all members' commit history | [Final-Project-SDP](https://github.com/ANAYSSA/Final-Project-SDP); merge commits preserve all three authors' work. |
| Report with introduction, body, conclusion and further work | The updated [REPORT.pdf](REPORT.pdf) is a four-page report. |
| Java code, application screenshots and UML | Included in the report; editable UML and larger figures are in [uml/](uml/README.md). |
| PDF report and source ZIP submitted to Moodle | Every student must upload the final report and source ZIP, and provide the GitHub link. A link does not replace the ZIP. |
| No late submission | Check the actual Moodle deadline and confirm each student's upload. No deadline date was supplied with the project material. |
| Oral defense on OOP and patterns | Rehearse the assigned patterns, their interactions and the application limits. Defense completion is not established by the code or commits. |

## Package structure

```text
src/main/java/com/restaurant/
├── Main.java
├── builder/
├── factory/
├── strategy/
├── bridge/
├── observer/
└── decorator/
```

Pattern roles have their own subfolders. The order application, DTOs and HTTP adapters sit under `builder` to preserve the course's six-package rule; those adapters are not Builder participants. Tests use the corresponding pattern packages under `src/test/java`.

## Checks before hand-in

1. Use the final merged revision. Run the Maven checks, package check and `--demo` commands in README.
2. Demonstrate FAMILY pricing, herbs, customer/kitchen receipts in text, HTML and JSON, and all three observer audiences.
3. Check that the report, screenshots, UML and source ZIP match the submitted revision. See [VALIDATION.md](VALIDATION.md) for verification evidence.
4. Each student submits the PDF, source ZIP and repository link before the course deadline and verifies their own upload.

The app is a demonstration: no real payment or kitchen service is connected, and session data is lost after expiry or server restart. Hosting the site supports the defense; it does not replace the submission files.

## Grading

| Component | Weight |
| --- | ---: |
| Project code and Git link | 50% |
| Project report | 10% |
| Defense: OOP and patterns | 40% |

The additional instructions state a 5% deduction per violation, including core PDF requirements.
