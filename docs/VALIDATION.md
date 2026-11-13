# Verification evidence

Verified on 13 November 2026 with JDK 21 and Maven 3.9.11.

## Automated checks

`./mvnw --batch-mode --no-transfer-progress verify` completed successfully.

| Suite | Tests | Failures / errors |
| --- | ---: | ---: |
| OrderApiTest | 11 | 0 |
| ConcurrentOrderTest | 1 | 0 |
| OrderBuilderTest | 3 | 0 |
| MealPatternTest | 3 | 0 |
| PricingTest | 2 | 0 |
| ReceiptBridgeTest | 2 | 0 |
| OrderProgressTest | 1 | 0 |
| **Total** | **23** | **0** |

`python3 scripts/check_structure.py` passed: six pattern packages, matching package declarations and only `Main.java` at the root. `node --check src/main/resources/static/app.js` passed. All bundled SVGs parsed as XML.

`java -jar target/hearth-and-table-1.0.0.jar --demo` exited 0 and exercised all six patterns. Example output: two Margheritas with cheese and olives cost 5300.00 KZT, pickup discount is 1060.00, total is 4240.00; both document purposes work with both renderers; eight lifecycle notifications are produced.

The first GitHub Actions run revealed a wrapper checksum mismatch: a ZIP download was checked against a tarball checksum. Commit `5a35a60` corrects the checksum and was verified with a fresh wrapper cache. [The subsequent CI run passed](https://github.com/ANAYSSA/Final-Project-SDP/actions/runs/43426440809).

## Browser checks

The local build and deployed HTTPS application were both exercised through the visible browser.

- Added Margherita with extra cheese, quantity 2.
- Selected dine-in, table 7, a customer name, a preparation note and STUDENT discount.
- Quote and placed order both showed 4800 subtotal, 480 discount, 4320 total.
- Advanced PLACED → PREPARING → READY; both customer and kitchen notifications appeared.
- Opened the customer HTML receipt and checked its total and current status. Java integration tests cover all four receipt combinations and session ownership.
- Confirmed the final UI no longer repeats topping descriptions in placed orders.
- Tested a 390 × 844 mobile viewport: no horizontal overflow (content width 390). Drink customization has no topping options; takeaway with PICKUP gives 700 − 140 = 560 KZT and hides the table field.
- Browser console error collection was empty during the exercised flow.

Screenshots in `docs/screenshots/` are actual captures, not mockups. `menu.jpg` and `orders.jpg` show the deployed build; `mobile.jpg` records the local responsive check. The final report contains the deployed screenshots.

## Deployment

Public HTTPS URL: https://hearth-table.178.62.244.88.sslip.io

The live `/api/health` endpoint returned `{"status":"UP"}` with certificate verification enabled. A full order was created and advanced through the public URL, confirming secure session cookies and reverse-proxy behavior. The JAR contains the application source from `954a3c8`; the following wrapper correction changes only how Maven is downloaded, not application behavior.

The dedicated `hearth-and-table` Compose project runs beside the pre-existing service. Both new containers and the existing bot were running after deployment. No new paid infrastructure resource was created.

## Remaining submission work

These checks do not establish individual teammate authorship or Moodle submission. Akmaral and Mukhammad must complete their two-pattern tasks under their own identities, have the PRs reviewed and merged, and update this evidence, report and source ZIP. The deadline must be checked in the course system. All students must submit individually.
