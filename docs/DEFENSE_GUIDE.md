# Defense guide

## Opening explanation

“Hearth & Table is a restaurant ordering application. HTML, CSS and JavaScript provide the interface, and Java with Spring Boot handles validation and order behavior. Six explicit design patterns cooperate in one flow: Factory Method creates menu products, Decorator adds toppings, Builder constructs the order, Strategy calculates its discount, Observer records status notifications, and Bridge renders receipts for different audiences and formats.”

The application simulates restaurant ordering and kitchen progress. It does not charge a payment card or claim that an actual restaurant has prepared food. The initial baseline was authored under Anuar's Git identity; use the final Git history and contribution documents to describe subsequent individual work accurately.

## Six patterns in this project

| Pattern | Category and roles | Why it is useful here | Evidence to show |
| --- | --- | --- | --- |
| Builder | Creational: product, builder, optional director/client | Orders have required data, optional notes and multiple items. Construction can validate the complete configuration before returning an order. | Builder calls; validation; an invalid order rejected before storage |
| Factory Method | Creational: product interface, concrete products, creator and concrete creators | The client requests a product through a creation contract; concrete creators decide which meal object to instantiate. | Creator method override and two concrete creators; the menu-to-creator registration |
| Strategy | Behavioral: strategy contract, concrete algorithms, pricing context | The same order can be priced using regular, student or pickup rules without duplicating the order flow. | Context delegation; quote totals with different eligible modes |
| Bridge | Structural: receipt abstraction, refined abstractions, renderer implementor and concrete renderers | Receipt purpose (customer/kitchen) changes independently of output format (text/HTML). | Both receipt purposes rendered by the same two renderer implementations |
| Observer | Behavioral: subject, observer contract, concrete subscribers | A successful order-state change notifies customer and kitchen subscribers without coupling the subject to their concrete classes. | Subscription and publish calls; notifications after a valid transition |
| Decorator | Structural: component, concrete component, base decorator, concrete decorators | Toppings add price and description by wrapping an existing meal. Several toppings can be composed at runtime. | A wrapped meal's price and labels; stacked toppings |

Use the actual class names shown in the final source and UML when answering questions. New teammate extensions, such as FAMILY, JSON rendering, herbs and an operations subscriber, should be demonstrated only after they have been implemented and tested.

## Questions that distinguish similar patterns

**Builder versus Factory Method.** Builder assembles one configured object through several construction steps. Factory Method delegates the choice of a product's concrete class to a creator. In this project, creating a menu product and assembling an order are different responsibilities.

**Factory Method versus a simple factory.** A switch that calls constructors is a selection helper, not enough by itself to demonstrate the GoF Factory Method structure. Point to the creator contract and overridden creation methods. The registry may select a creator; that selection is not the whole pattern.

**Bridge versus Strategy.** Strategy selects one algorithm through an interface. Bridge separates two dimensions that can vary independently: the receipt's purpose and its renderer. Show the two hierarchies and their composition; merely having an interface does not prove Bridge.

**Decorator versus subclass combinations.** With inheritance, every combination of toppings can require another subclass. A decorator wraps the component contract, so cheese and mushrooms can be composed without a `CheeseMushroomPizza` class. All wrappers retain the component's expected behavior.

**Observer versus browser polling.** Observer happens inside Java when the order subject notifies its subscribers. A browser fetching the latest events is a delivery mechanism. Polling alone does not demonstrate Observer, and an Observer implementation does not automatically mean WebSocket push.

**Why not use every pattern studied?** Each selected pattern solves a stated problem. Facade and Visitor appeared in earlier assignments, but the chosen six cover the current design. Extra pattern names do not improve the defense unless their actual structure and purpose can be demonstrated.

## OOP questions

- **Encapsulation:** keep object state private; expose controlled operations. Order validation and permitted status changes protect invariants.
- **Abstraction:** depend on a meal, strategy, renderer or observer contract without requiring callers to understand each implementation.
- **Inheritance:** a concrete creator or refined receipt can extend a suitable base type. Use inheritance for a meaningful substitutable relationship.
- **Polymorphism:** the same contract call invokes the selected implementation, such as the current discount algorithm or renderer.
- **Composition:** an object contains collaborators, such as a receipt holding its renderer or a topping wrapping its meal. This allows independent replacement.
- **Interface versus abstract class:** an interface specifies a contract; an abstract class can also share state and implementation. Choose based on the actual common behavior, not to increase the class count.
- **Overloading versus overriding:** overloading uses different parameter lists; overriding replaces inherited behavior with the same compatible method signature. Factory Method relies on overriding.
- **Immutability:** an immutable value cannot be changed after construction. Defensive copies prevent callers from mutating an order's internal item collection.

## Clean Code and SOLID

| Principle | Explain using the project | Boundary to acknowledge |
| --- | --- | --- |
| Single Responsibility | Builders construct, strategies price, renderers format and observers react. | A controller should translate HTTP requests; it should not calculate topping prices. |
| Open/Closed | Add a concrete strategy, renderer, topping or subscriber through the existing contract. | Registering a new implementation may require a small composition/configuration change. |
| Liskov Substitution | Every meal decorator remains usable through the meal contract; every renderer satisfies the same rendering contract. | Do not weaken required guarantees or surprise callers with unsupported behavior. |
| Interface Segregation | Keep strategy, observer and renderer contracts small and relevant to their clients. | One interface containing unrelated pricing, printing and notification methods would violate this aim. |
| Dependency Inversion | High-level behavior delegates to strategy, renderer and observer abstractions. | The composition root must still choose concrete implementations. |
| KISS | A small monolithic Spring Boot app, ordinary Java objects and a plain frontend are sufficient for this demonstration. | Avoid adding infrastructure that the course scenario does not need. |
| DRY | The backend owns price/topping/discount calculations and exposes menu data to the frontend. | Similar-looking code is not always the same business rule; avoid premature generalization. |
| Clean Code | Descriptive names, small methods, explicit validation, consistent formatting and useful errors. | Show source examples and tests instead of claiming that code is “fully SOLID.” |

## Eight-minute demonstration

1. **Context, 30 seconds.** State the problem, stack, team allocation and six patterns. Open the running website and the repository.
2. **Factory Method and Decorator, 90 seconds.** Select Margherita at 2000 KZT. Add cheese (400) and mushrooms (300): one unit is 2700 KZT. Show the creation contract and the wrapper chain in Java.
3. **Builder and Strategy, 90 seconds.** Set quantity to two: subtotal 5400 KZT. Choose STUDENT: discount 540, total 4860. Fill valid customer and fulfillment details and place the order. Show Builder validation and strategy delegation. PICKUP is valid only with TAKEAWAY.
4. **Observer, 60 seconds.** Advance the demo order from PLACED to PREPARING to READY to COMPLETED. Show the events and Java notification call. Explain that progress is explicitly simulated. A further advance must be rejected without generating a false event.
5. **Bridge, 60 seconds.** Open the same order's customer receipt in text and HTML, then the kitchen receipt in both formats. Show the two axes of the class diagram and the renderer reference inside the receipt abstraction.
6. **Robustness, 60 seconds.** Demonstrate a useful invalid-input response and session isolation. Explain that the backend computes prices, a repeated creation request is idempotent, and errors do not expose stack traces. Use tests for scenarios awkward to trigger through the UI.
7. **Evidence, 90 seconds.** Run the documented Maven verification and `--demo` commands, show the six-package tree, relevant UML and factual Git contribution history. Explain actual limitations and proposed further work.

If the available defense time is shorter, keep one integrated order flow and spend less time switching between files. Each student should explain their two patterns and answer a question about how they connect to the rest of the application.

## Prepare before presenting

- Use the final tested revision, and make sure the report's screenshots and class names match it.
- Rehearse the exact startup, Maven verification and `--demo` commands from README.
- Keep local startup available in case the demonstration host is unreachable.
- Read the actual storage implementation and explain its restart behavior accurately. Do not promise durable storage, production authentication or real payment processing unless those features exist and were verified.
- Be ready to trace one request from HTTP validation through the Java objects and back to the returned receipt.
- Describe further work as future work: production authentication, durable storage if absent, real kitchen access, payment-provider integration, inventory and stronger operational monitoring.
