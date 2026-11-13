# Mukhammad — Observer and Decorator

This contribution extends the existing restaurant ordering application on
`feature/mukhammad-observer-decorator`, based on main commit
`65674834e28ed231c750ffb2e6572fa72ebfa745`. It adds Herbs decoration and a
session-scoped operations event projection. The existing pattern foundation,
other students' work, and original tests are preserved.

## Decorator extension

- `decorator.concrete.HerbsDecorator` wraps any eligible `Meal` through the
  existing `ToppingDecorator` contract. It adds 150 KZT per unit and a Herbs
  label, delegates identity/category, and inherits the prohibition on drinks.
- `decorator.component.ToppingType.HERBS` defines metadata for `herbs` and is
  the single production source of its Herbs display name and 150.00 KZT price.
- `decorator.catalog.ToppingCatalog` registers the constructor for `herbs`.
  Its existing metadata projection makes Herbs available in `GET /api/menu`;
  quote and placement use the same decorated meal prices.

Margherita with Herbs costs 2150 KZT, or 4300 for quantity two. Herbs and
cheese produce 2550 KZT per unit in either wrapping order. Herbs composes
with mushrooms and olives too, including on pasta. The HTTP boundary still
allows at most three distinct topping IDs per line and rejects duplicate,
unknown, blank, null, malformed, and otherwise invalid topping inputs.
The general component contract can compose all four decorators; that does
not relax the API's existing three-topping limit.

Decorator uses runtime object composition: a wrapped `Meal` is itself a
`Meal`, so extra behavior stacks without a subclass for every combination.
`HerbsDecorator` inherits the shared wrapper implementation, but the changing
combination comes from delegation to another object, not a growing hierarchy
of classes such as `MargheritaWithHerbsAndCheese`.

## Observer extension and lifecycle

- `observer.concrete.OperationsDashboardObserver` implements `OrderObserver`.
  Its only retained state is a final positive aggregate item quantity,
  supplied through its constructor. It never receives an `Order`.
- `builder.application.OrderService` sums quantities across the immutable
  order lines and subscribes one Operations observer before `announce()`.
- The existing `OrderProgress` subject, `StatusChange`, and observer contract
  remain unchanged. The subject calls the interface without knowing the new
  concrete observer type and stores the returned notifications in memory.
- `builder.application.dto.OrderView` adds `operationsEvents` to the same
  session-scoped response. The existing `events` field retains its
  customer/kitchen feed, preserving clients and other students' tests.
- `static/app.js` renders the operations projection within each existing order
  card. No topping prices or calculation rules were added to JavaScript.
- `builder.application.PatternDemo` prints and verifies all three audiences.

Placement publishes `PLACED` once, then successful advances publish
`PREPARING`, `READY`, and `COMPLETED`. Rejected advances and duplicate
announcements add nothing. The subject's existing subscription set prevents
duplicate registration of the same observer instance. Unsubscribing that
Operations instance stops its future events; historical events remain and
customer/kitchen subscribers continue. Each order owns its subject and
subscribers in the existing session order book. Restart/session expiration
removes this in-memory state; there is no external logging or delivery service.

Observer is synchronous publication from the domain subject when state changes.
Refreshing the browser only reads the stored projection; polling neither
creates notifications nor replaces the Observer pattern. The existing
request-ID lookup runs before order creation/subscription under the order-book
lock, so both sequential and concurrent retries return the existing order.

## Event privacy and access

An Operations message has the form `Order ABCDEF12 | READY | Items: 5`.
The reference matches the browser's public eight-character uppercase order
label. The notification contains the existing status, audience `OPERATIONS`,
and publication timestamp. It contains no customer name, table, note, request
ID, session ID, token, item details, pricing, or receipt.

The owner's surrounding order response still contains its normal order
details; the privacy guarantee here applies to the operations event projection.
Existing session authorization protects both order details and events. Another
session cannot list, read, advance, or obtain a receipt for the order. No global
dashboard endpoint or cross-session order store was introduced.

## Verification

Verified on Java 21.0.13 with Maven wrapper 3.9.11:

```sh
./mvnw -Dtest=HerbsDecoratorTest,HerbsApiTest,OperationsDashboardObserverTest,OperationsDashboardApiTest test
./mvnw verify
java -jar target/hearth-and-table-1.0.0.jar --demo
python3 scripts/check_structure.py
node --check src/main/resources/static/app.js
git diff --check
```

- Targeted run: 38 tests passed, zero failures/errors/skips.
- Full README verification command: 61 tests passed, zero failures/errors/skips;
  executable application JAR built successfully.
- Console demo: all six patterns passed; four Operations events accompany
  the eight existing customer/kitchen events.
- Package and JavaScript syntax checks passed.
- Chromium browser smoke passed: selected Herbs through the menu, verified
  quantity two at 4300 KZT and PICKUP total 3440 KZT, placed the order, and
  advanced through all statuses. The UI displayed four Operations and eight
  customer/kitchen events; rejected advances preserved events and a fresh
  browser session could not read the order. No JavaScript errors occurred.
- New tests are `decorator.HerbsDecoratorTest`, `decorator.HerbsApiTest`,
  `observer.OperationsDashboardObserverTest`, and
  `observer.OperationsDashboardApiTest`. They cover composition, quantities,
  all existing discounts, invalid API inputs, publication counts, unsubscribe,
  rejected transitions, sequential/concurrent idempotency, and privacy/session
  isolation. No existing test file was edited.

The cloud workspace initially had a Java runtime without a compiler. A JDK
was installed outside the repository; Maven used local settings for the
environment's inherited proxy and system Java truststore. No environment
settings, downloaded dependencies, generated JARs, or logs belong in this PR.

## Implementation commits

- Decorator: `1a5b0716c360b9406d0b5a3abea7380eb91d680e` — Herbs metadata,
  decorator, registration, and unit/API coverage.
- Observer: `67abc2aa28a7934743811b098acc2beca18cf047` — Operations subscriber,
  session projection, UI/demo integration, and lifecycle/privacy/retry tests.

Before committing, Git author configuration was inspected as
`Vertex <v24169168@gmail.com>` and the authenticated GitHub login as
`Vertex-blip` (profile name `vertex`). The existing identity was used unchanged;
no author override, global identity change, backdating, or empty commit was used.
This document is a subsequent documentation commit so it can record the actual
implementation hashes. The reviewed diff contains source/tests/documentation
only; no common credential patterns or generated files were found.

## Defense explanation

“My Decorator adds Herbs by wrapping a Meal, so pricing and labels compose
while identity and category stay intact. My Observer subscribes through the
existing interface and receives each published status exactly once. It keeps
only the aggregate quantity and emits a minimal Operations event. The subject
does not depend on the new concrete class, and the projection stays inside the
owner's session. Tests demonstrate composition, privacy, idempotency, and
unsubscribe behavior.”
