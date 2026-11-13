# UML diagrams

These diagrams describe the Java implementation in `src/main/java/com/restaurant/`. They show the main pattern roles and selected methods rather than every accessor or helper. Names are taken from the actual source; future contribution tasks are not depicted as completed features.

| Diagram | Source | Rendered copies |
| --- | --- | --- |
| Builder: `Order.Builder` constructs `Order` | [builder.puml](builder.puml) | [PNG](builder.png), [SVG](builder.svg) |
| Factory Method: `MealFactory` and concrete creators/products | [factory.puml](factory.puml) | [PNG](factory.png), [SVG](factory.svg) |
| Strategy: `PricingCalculator` delegates to `DiscountStrategy` | [strategy.puml](strategy.puml) | [PNG](strategy.png), [SVG](strategy.svg) |
| Bridge: receipt purpose and renderer hierarchies | [bridge.puml](bridge.puml) | [PNG](bridge.png), [SVG](bridge.svg) |
| Observer: `OrderProgress` notifies subscribed `OrderObserver`s | [observer.puml](observer.puml) | [PNG](observer.png), [SVG](observer.svg) |
| Decorator: `ToppingDecorator` wraps `Meal` | [decorator.puml](decorator.puml) | [PNG](decorator.png), [SVG](decorator.svg) |
| Integrated order flow | [integrated-sequence.puml](integrated-sequence.puml) | [PNG](integrated-sequence.png), [SVG](integrated-sequence.svg) |

The integrated sequence diagram is a concise view of calls coordinated by `OrderService`. It omits HTTP/session translation, storage, DTO conversion and repeated item/topping calls. Notes expand the construction and pricing delegation. The implementation checks idempotency before creating a second order and resolves receipt/order access against the current session's `OrderBook`; these checks are not drawn as pattern participants.

To regenerate, install Java and download the [PlantUML JAR](https://plantuml.com/download) from the official distribution or Maven Central. From the repository root, replace `/path/to/plantuml.jar` with its location:

```sh
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -tpng docs/uml/*.puml
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -tsvg docs/uml/*.puml
```

The class diagrams use PlantUML's embedded Smetana layout, so no Graphviz executable is required. These copies were generated with PlantUML 1.2026.10 and Java 21. Use PNGs in the report and SVGs for zoomable source review. Re-render and inspect the output whenever a diagram changes; update the report after teammate extensions modify the implemented classes.
