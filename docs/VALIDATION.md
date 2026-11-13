# Verification evidence

Verified on 13 November 2026 with JDK 21 and Maven 3.9.11. The deployed application uses runtime revision `845a9bf`, after both teammate PRs were merged.

## Automated checks

`./mvnw --batch-mode --no-transfer-progress verify` passed. The table below is taken from the Surefire XML results for the combined application.

| Suite | Tests | Failures / errors | Skipped |
| --- | ---: | ---: | ---: |
| JsonReceiptApiTest | 4 | 0 | 0 |
| JsonReceiptRendererTest | 5 | 0 | 0 |
| ReceiptBridgeTest | 2 | 0 | 0 |
| ConcurrentOrderTest | 1 | 0 | 0 |
| OrderApiTest | 11 | 0 | 0 |
| OrderBuilderTest | 3 | 0 | 0 |
| HerbsApiTest | 19 | 0 | 0 |
| HerbsDecoratorTest | 7 | 0 | 0 |
| MealPatternTest | 3 | 0 | 0 |
| OperationsDashboardApiTest | 5 | 0 | 0 |
| OperationsDashboardObserverTest | 7 | 0 | 0 |
| OrderProgressTest | 1 | 0 | 0 |
| FamilyDiscountApiTest | 8 | 0 | 0 |
| FamilyDiscountTest | 10 | 0 | 0 |
| PricingTest | 2 | 0 | 0 |
| **Total** | **88** | **0** | **0** |

The package structure check and JavaScript syntax check passed. `Main.java` remains the only Java source directly in `com.restaurant`; the other classes are under the six pattern packages.

`java -jar target/hearth-and-table-1.0.0.jar --demo` passed and exercised all six patterns. The console scenario retains the 5300.00 KZT subtotal and 4240.00 KZT pickup total, checks receipt formats, and finishes with eight customer/kitchen notifications plus four operations notifications.

GitHub Actions runs Maven verification, the console demonstration and the package check on pushes and pull requests. [The deployed revision passed CI](https://github.com/ANAYSSA/Final-Project-SDP/actions/runs/43431254490).

## Integration and author history

- [PR #1](https://github.com/ANAYSSA/Final-Project-SDP/pull/1): Akmaral's FAMILY strategy (`1debe3f`) and JSON renderer (`7475264`), merged as `e505627`.
- [PR #2](https://github.com/ANAYSSA/Final-Project-SDP/pull/2): Mukhammad's Herbs decorator (`f51cc9c`) and operations observer (`b2e5b55`), merged as `a91e948`.

Both PRs were reviewed and merged with merge commits, preserving their original authors. The combined tests cover price boundaries and rounding, invalid input, idempotent retries, concurrent requests, session isolation, receipt escaping, observer privacy, unsubscribe and terminal-state rejection.

GitHub links Akmaral's implementation commits to `arynttai` and Mukhammad's corrected implementation commits to `Vertex-blip`. Mukhammad's author email was corrected at the project owner's request; the three commit messages, dates and code trees were preserved. This changed their hashes and the hashes of descendant commits. A local history bundle was saved before the correction. The Contributors display may take time to refresh.

For traceability, the original PR #2 merge `c4d6ff5` corresponds to `a91e948` after the email correction; original runtime commit `6611d5a` corresponds to `845a9bf`. Their source trees are identical. The CI run linked above tested those same runtime contents before the metadata correction.

## Browser checks

The updated application was exercised through its public HTTPS URL.

- Added three Margheritas with Herbs: subtotal 6450.00 KZT.
- Selected FAMILY: discount 967.50 KZT; quote and placed order total 5482.50 KZT.
- Advanced the order to READY and checked customer, kitchen and operations events.
- Opened the JSON receipt and checked its content and total.
- Confirmed that the interface enforces the maximum of three toppings per line.
- Checked a 390 x 844 mobile viewport: content width was 390, with no horizontal overflow.
- Browser console error collection was empty during the exercised flow.

`docs/screenshots/menu-final.jpg` and `docs/screenshots/orders-final.jpg` are captures of the updated live application used in the four-page report. Older screenshot files are retained as earlier project records.

## Deployment

Public HTTPS URL: https://hearth-table.178.62.244.88.sslip.io

The updated JAR runs on the existing DigitalOcean Droplet in the dedicated `hearth-and-table` Compose project. A public order and its status changes were verified after deployment. Session cookies use Secure, HttpOnly and SameSite=Lax settings. The server's other service remains separate; no new paid server was created.

The local and deployed JAR SHA-256 values matched: `5c686b19de764652fd25c159f11edf002303f14a9171297c4164673701b4f798`. The public health endpoint returned `{"status":"UP"}`. Both application containers and the pre-existing bot were running after deployment.

Orders and events remain in session memory. They expire with the session or are cleared by a server restart. Status advancement is a demonstration control, and no real payment or restaurant service is connected.

## Submission

The software contributions are merged and verified. Each student must still submit the matching PDF report, source ZIP and GitHub link in Moodle by the course deadline and complete the oral defense. These checks do not claim that either student submission or the defense has already happened.
