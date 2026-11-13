# Akmaral — Strategy and Bridge contribution

Branch: `feature/akmaral-strategy-bridge`, started from `main` at `6567483`.

This work was done in an AI-assisted development session (Claude Code), as described in [TEAM_TASKS.md](../TEAM_TASKS.md). The commits are authored under my own Git identity and carry a `Co-Authored-By` trailer for the assistant. I reviewed the changes and must be able to explain them at the defense.

| Commit | Content |
| --- | --- |
| `1debe3f` | Strategy: `FamilyDiscount`, `DiscountCatalog` registration, Strategy tests |
| `7475264` | Bridge: `JsonReceiptRenderer`, `format=json` in the receipt API, Bridge tests, console demo check |
| *(this file)* | Documentation; its hash is in the pull request, since a commit cannot contain its own hash |

## 1. Strategy: FAMILY discount

### Behaviour

- Code `FAMILY`, menu name "Family - 15%".
- 15% off the **decorated item subtotal**: base price + toppings, multiplied by quantity, summed over lines. This is the same subtotal `PricingCalculator` already computes for all strategies.
- Valid for `DINE_IN` and `TAKEAWAY`.
- Requires subtotal ≥ **6000.00 KZT**. Eligibility depends only on that subtotal, never on clock time.
- Below the threshold the request is rejected with HTTP 400 and a message such as
  `Family discount requires a subtotal of at least 6000.00 KZT; this order is 5600.00 KZT`.
  No order is created, and no other strategy is applied in its place.
- Cannot be stacked. An order carries exactly one `discountCode`, so FAMILY can't be combined with STUDENT or PICKUP. Combined codes such as `FAMILY,STUDENT` are rejected as unknown codes.

### Rounding policy

The project rule is unchanged and lives in the context (`PricingCalculator`), not in the strategy:

1. Subtotal = sum of line totals, then `setScale(2, RoundingMode.HALF_UP)`.
2. Discount = `strategy.discount(subtotal, fulfillment)`, then `setScale(2, RoundingMode.HALF_UP)`.
3. Total = subtotal − discount.

`FamilyDiscount` returns the unrounded `subtotal × 0.15`, as `StudentDiscount` and `PickupDiscount` do. A test pins the rule: 6000.30 × 0.15 = 900.045 → **900.05** (HALF_EVEN would give 900.04).

### Classes changed

| Class | Change |
| --- | --- |
| `strategy/concrete/FamilyDiscount.java` | **New** concrete strategy implementing `DiscountStrategy` |
| `strategy/context/DiscountCatalog.java` | Small refactor + registration of FAMILY (see below) |

`PricingCalculator`, `DiscountStrategy`, `Order`, `OrderService` and the controller are **not changed**. The quote flow (`OrderService.quote`) and the place-order flow (`Order.Builder.build` → `PricingCalculator`) both resolve the strategy through `DiscountCatalog.find` and call it only through the `DiscountStrategy` contract. There is no `if (FAMILY)` branch anywhere.

### Why the small `DiscountCatalog` refactor

Before, each discount was written in two places: a `Map.of("CODE", strategy)` for lookup, and a separate hard-coded `List<DiscountOption>` for `GET /api/menu`. Adding FAMILY would have meant a third and fourth copy of the code string, and nothing stopped the two lists from drifting apart. The catalog now has one `register(strategy, name, description)` call per strategy. The key and option id both come from `strategy.code()`, and duplicate codes fail fast. The public JSON shape (`id`, `name`, `description`) and the order and text of NONE, STUDENT and PICKUP are unchanged. A future discount is one new class plus one `register` line.

The frontend already builds the discount dropdown from `/api/menu` and shows quote errors, so FAMILY appears there with no JavaScript change.

## 2. Bridge: JSON receipt renderer

### Behaviour

`GET /api/orders/{id}/receipt?format=json&type=customer|kitchen` returns `Content-Type: application/json`:

```json
{"title":"Hearth & Table · Kitchen Ticket","lines":["Ticket: …","Service: DINE_IN · Table 3","Status: PLACED","2 × Basil Pesto + Extra cheese","Preparation note: a\nb"]}
```

- `format=text` and `format=html` work as before, and `html` is still the default. `type` still defaults to `customer`.
- Unsupported formats (`pdf`, `xml`, `JSON`, `jsonp`, …) return 400 `Receipt format must be text, html or json`.
- The order is still looked up in the caller's session `OrderBook`, so another session (or no session) gets 404. No new authorization path was added.

### Classes changed

| Class | Change |
| --- | --- |
| `bridge/implementation/JsonReceiptRenderer.java` | **New** concrete implementor of `ReceiptRenderer` |
| `builder/application/OrderService.java` | One `case "json"` in the existing format switch and the updated error message |
| `builder/application/PatternDemo.java` | `--demo` also checks the JSON receipt and mentions it |

`ReceiptDocument`, `CustomerReceipt`, `KitchenTicket`, `ReceiptContent`, `ReceiptRenderer`, and the text and HTML renderers are **not changed**.

### How the Bridge keeps purpose and format independent

- **Abstraction side** (`ReceiptDocument` → `CustomerReceipt`, `KitchenTicket`) decides *what* is on the document. The kitchen ticket never includes the guest name or prices. Because content is built once there, the JSON kitchen ticket follows the same rules automatically. A test proves the JSON lines equal the text renderer's lines for both purposes.
- **Implementor side** (`ReceiptRenderer` → `Text`, `Html`, `Json`) decides *how* `ReceiptContent` is encoded. It knows nothing about orders or purposes.
- Adding the JSON format took **one** class, not one per document type (no `CustomerJsonReceipt` and `KitchenJsonReceipt`). With 2 purposes × 3 formats we have 2 + 3 classes instead of 6.
- `JsonReceiptRenderer` uses Jackson (`ObjectMapper`), which the project already ships with Spring Web. Quotes, backslashes, newlines, ` ` and `</script>` in user text are escaped by the library, not by hand-written string concatenation.

## Strategy versus Bridge

| | Strategy | Bridge |
| --- | --- | --- |
| Category | Behavioural | Structural |
| Question it answers | *Which algorithm* computes the discount? | How do two independent dimensions (purpose × format) combine without a class explosion? |
| Varies | One operation (`discount`) chosen per order at run time | Two hierarchies, linked by composition (`ReceiptDocument` *has a* `ReceiptRenderer`) |
| Client | `PricingCalculator` (context) depends only on `DiscountStrategy` | `ReceiptDocument.render` delegates to the `ReceiptRenderer` it holds |
| My extension | New algorithm `FamilyDiscount` | New implementor `JsonReceiptRenderer` |

Both rely on composition and an interface (Open/Closed, Dependency Inversion). The difference is intent. Strategy swaps one behaviour. Bridge separates an abstraction hierarchy from an implementation hierarchy so both can grow independently.

## Tests

New test classes (existing tests were not modified):

| Test class | What it checks |
| --- | --- |
| `strategy/FamilyDiscountTest` (10) | 5999 and 5999.99 rejected for both fulfillments with a useful message; 6000 → 900/5100 for both; 10000 → 1500/8500; HALF_UP rounding; toppings and quantity reach the threshold (pesto ×2 = 5200 rejected, pesto + cheese ×1 = 3000 rejected, ×2 = 6000 accepted); registry ids match strategy codes; combined codes rejected; NONE, STUDENT and PICKUP unchanged |
| `strategy/FamilyDiscountApiTest` (8) | `/api/menu` lists NONE, STUDENT, PICKUP, FAMILY; quote equals placed order for FAMILY dine-in (6000) and takeaway (10000); below threshold → 400 on quote and order, and no order stored; combined codes → 400; NONE, STUDENT and PICKUP quote = order |
| `bridge/JsonReceiptRendererTest` (5) | One renderer instance used by both documents through `ReceiptDocument`; customer JSON round-trips quotes, newlines and markup exactly; kitchen JSON has no prices or guest name; JSON lines equal text-renderer lines per purpose; arbitrary hostile strings encode safely |
| `bridge/JsonReceiptApiTest` (4) | Both purposes over HTTP: parseable JSON, `application/json`, `no-store`, user text intact, kitchen rules; text and HTML content types unchanged; unsupported formats and types → 400; another session or no session → 404 |

Commands run (Windows, Git Bash):

```sh
./mvnw -B test -Dtest='com.restaurant.strategy.*Test'   # 20 tests, 0 failures
./mvnw -B test -Dtest='com.restaurant.bridge.*Test'     # 11 tests, 0 failures
./mvnw -B verify                                        # 50 tests, 0 failures, BUILD SUCCESS
java -jar target/hearth-and-table-1.0.0.jar --demo      # PASS — all six patterns exercised successfully
python scripts/check_structure.py                       # PASS: six pattern packages ...
```

Baseline before my changes: 23 tests, all passing. After: 50 (23 existing + 27 new), all passing. I also started the jar locally and checked with `curl` that FAMILY appears in `/api/menu`, a 6000 KZT FAMILY order totals 5100, JSON receipts return `Content-Type: application/json`, another session gets 404, and a 5600 KZT FAMILY quote returns 400.

**Environment note:** JDK 21 is not installed on my machine, so verification ran on **JDK 23** compiling with `--release 21` (`<java.version>21</java.version>` in the POM). The CI workflow runs on JDK 21 and is the authoritative check.

## Limitations and follow-ups

- The web UI does not link JSON receipts. TEAM_TASKS says JSON can be shown through the receipt URL with `format=json`, and frontend changes were out of scope.
- The UI lets you choose FAMILY for a small cart and then shows the server's validation message. It doesn't disable the option in advance, because the threshold lives only on the server (single source of truth).
- `format=` with an empty value falls back to the existing `defaultValue = "html"` (Spring behaviour, unchanged).
- UML (`docs/uml/strategy.puml`, `bridge.puml`), `REPORT.pdf` and `DEFENSE_GUIDE.md` don't show `FamilyDiscount` or `JsonReceiptRenderer` yet. Regenerating them is part of the integration and report refresh after merge.
- Orders are still in-memory and per session, as in the baseline.
