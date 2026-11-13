# Defense guide

## Opening explanation

Hearth & Table is a restaurant ordering application. The browser handles the interface; Java and Spring Boot validate orders and calculate their prices. Six patterns handle different parts of the same flow: Factory Method creates meals, Decorator adds toppings, Builder assembles an order, Strategy applies a discount, Observer handles status notifications, and Bridge renders receipts.

The kitchen status control is a simulation. There is no real payment processor or restaurant connection. Orders are held in the current HTTP session and reset after session expiry or a server restart.

## Pattern roles

| Pattern | Main classes | What to demonstrate |
| --- | --- | --- |
| Builder - Anuar | `Order.Builder`, `Order`, `OrderLine` | Fluent construction, required fields, validation and the immutable order product. |
| Factory Method - Anuar | `MealFactory`, `PizzaFactory`, `PastaFactory`, `DrinkFactory`, `Meal` | `create()` delegates to the overridden `createMeal()` operation. |
| Strategy - Akmaral | `DiscountStrategy`, `PricingCalculator`, `NoDiscount`, `StudentDiscount`, `PickupDiscount`, `FamilyDiscount` | The context delegates to one selected rule. FAMILY gives 15% off a subtotal of at least 6000 KZT. |
| Bridge - Akmaral | `ReceiptDocument`, `CustomerReceipt`, `KitchenTicket`, `ReceiptRenderer` and its text/HTML/JSON implementations | Two document purposes work with three output formats through composition. |
| Observer - Mukhammad | `OrderProgress`, `OrderObserver`, `CustomerObserver`, `KitchenObserver`, `OperationsDashboardObserver` | Every valid transition produces customer, kitchen and operations events. Unsubscribing stops future events for that subscriber. |
| Decorator - Mukhammad | `Meal`, `ToppingDecorator`, `ExtraCheese`, `Mushrooms`, `Olives`, `HerbsDecorator` | Wrappers add price and description while preserving the meal interface and category. Herbs costs 150 KZT per unit. |

Akmaral's implementations were merged in [PR #1](https://github.com/ANAYSSA/Final-Project-SDP/pull/1), and Mukhammad's in [PR #2](https://github.com/ANAYSSA/Final-Project-SDP/pull/2). Use their contribution notes and original commits when explaining who changed which classes.

## One demonstration for all six patterns

1. Open the site and select Margherita at 2000 KZT. Add herbs at 150 KZT. Show the factory and decorator classes.
2. Set quantity to three: subtotal is 6450 KZT. Select FAMILY: discount is 967.50 KZT and total is 5482.50 KZT. Show `FamilyDiscount` and how the shared pricing context calls it.
3. Enter customer details and choose dine-in with a valid table, or takeaway. Place the order and show the Builder validation.
4. Advance PLACED to PREPARING, READY and COMPLETED. Each stage has two customer/kitchen events plus one operations event. After completion, there are eight events in the original feed and four operations events. The latter include only the public short reference, status and item quantity.
5. Open customer and kitchen receipts. Show text, HTML and JSON formats. The endpoint accepts `format=json&type=customer` or `format=json&type=kitchen`. Kitchen output omits the guest name and prices.
6. Show the test results, package structure and original author commits. Explain one invalid case: FAMILY below 6000, toppings on a drink, or another session requesting the order.

The API permits up to three selected toppings per line. Discount codes do not stack. Prices use `BigDecimal` with scale 2 and `HALF_UP` rounding. Retrying the same creation request ID with the same payload returns the same order rather than creating a duplicate.

## Common questions

**Builder versus Factory Method:** Builder assembles a configured object in steps; Factory Method delegates creation of a concrete product to a creator subclass.

**Factory Method versus a simple factory:** A constructor switch alone is not the whole pattern. Show the abstract creator and the overridden creation method; the menu registry only selects the creator.

**Bridge versus Strategy:** Strategy swaps an algorithm. Bridge separates two independently variable dimensions, here document purpose and rendering format.

**Decorator versus inheritance:** Wrapping allows combinations such as cheese plus herbs without a separate subclass for every combination. The wrapper remains a `Meal`.

**Observer versus polling:** Observer dispatch happens in Java when the subject publishes a change. The browser reads the resulting events; fetching events is not itself the Observer pattern.

## OOP and clean code

- **Encapsulation:** private order state and controlled status transitions protect invariants.
- **Abstraction and polymorphism:** code calls `Meal`, `DiscountStrategy`, `ReceiptRenderer` and `OrderObserver` contracts; the selected implementation supplies the behavior.
- **Inheritance and overriding:** concrete creators override `createMeal()`; receipt subclasses define different document content.
- **Composition:** a decorator holds a meal, and a receipt holds a renderer.
- **Immutability:** the built order uses final fields and copies its item list.
- **Single responsibility:** creation, pricing, formatting and notification code have separate roles.
- **Open/closed:** new rules and renderers implement existing contracts; registration connects them to the application.
- **Liskov substitution:** meal wrappers and renderer implementations remain usable through their shared interfaces.
- **Interface segregation:** the contracts expose a small set of relevant operations.
- **Dependency inversion:** pricing, receipt abstractions and the subject use interfaces. The composition code selects concrete implementations.
- **DRY and KISS:** quote and order creation share server-side calculations; one small application is enough for this demonstration.

## Before presenting

Rehearse the actual classes and tests, not only their definitions. Keep local startup available if the host cannot be reached. Each student should explain their assigned pair and how it connects to the other patterns. Possible further work includes durable storage, staff authentication, inventory checks and real kitchen or payment integrations; these are not current features.
