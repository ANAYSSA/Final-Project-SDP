# Team contributions and completed tasks

Both teammate tasks below are complete and merged. The original prompts are retained as a record of the assigned work; they are not outstanding instructions.

| Member | Completed work | Commit evidence |
| --- | --- | --- |
| Anuar (`ANAYSSA`) | Initial application, Builder and Factory Method; integration and deployment | `954a3c8` and subsequent integration commits |
| Akmaral (`Akma`, GitHub `arynttai`) | FAMILY discount strategy, JSON receipt renderer and tests | `1debe3f`, `7475264`; [PR #1](https://github.com/ANAYSSA/Final-Project-SDP/pull/1), merged as `e505627` |
| Mukhammad (`Vertex`, GitHub `Vertex-blip`) | Herbs decorator, session-scoped operations observer and tests | `f51cc9c`, `b2e5b55`; [PR #2](https://github.com/ANAYSSA/Final-Project-SDP/pull/2), merged as `a91e948` |

The PRs were merged with merge commits, preserving the original authors. Detailed implementation notes remain in [AKMARAL.md](contributions/AKMARAL.md) and [MUKHAMMAD.md](contributions/MUKHAMMAD.md). See [VALIDATION.md](VALIDATION.md) for combined verification. Each member must still understand and defend their changes; commits do not establish that the oral defense or individual Moodle submission has happened.

## Completed task: Akmaral - Strategy and Bridge

Original task prompt, retained for the project record:

```text
I am Akmaral, a member of the Restaurant Ordering System final project team.
Repository: https://github.com/ANAYSSA/Final-Project-SDP
My assigned patterns are Strategy and Bridge.

Inspect the latest main branch, README, docs/REQUIREMENTS.md, current tests,
discount registry and receipt API before making changes. Read the actual
classes and preserve the existing API conventions. Work on a new branch
named feature/akmaral-strategy-bridge. Do not overwrite uncommitted work.

Implement two meaningful extensions:

1. Strategy: add a FAMILY discount strategy. It gives a 15% discount when
   the decorated item subtotal is at least 6000 KZT, for either DINE_IN or
   TAKEAWAY. Eligibility is based on explicit subtotal, never clock time.
   Reject a selected FAMILY discount below the threshold with a useful
   validation error; do not silently apply a different strategy. Do not
   stack it with STUDENT or PICKUP. Use the project's existing BigDecimal
   scale-2 HALF_UP rounding policy and document it. Register the new strategy so it appears
   in GET /api/menu and works in both quote and place-order flows. Keep the
   pricing context dependent on the strategy contract, not on a special
   FAMILY branch. Reuse existing registration/validation extension points;
   if a small refactor is required, explain why and preserve other modes.

2. Bridge: implement JsonReceiptRenderer (or the equivalent name following
   current conventions). Both CustomerReceipt and KitchenTicket must use
   it through the same renderer interface without duplicating receipt
   generation. Expose it through the existing receipt endpoint using
   format=json and Content-Type application/json. Preserve format=text
   and format=html and the type=customer|kitchen behavior. Use the existing
   JSON library to encode content safely. A kitchen receipt must retain
   the same purpose-specific content rules as other kitchen renderers.
   Keep receipt purpose and rendering format independently extensible.

Add meaningful tests for:
- FAMILY subtotal 5999: invalid; 6000: discount 900, total 5100.
- FAMILY subtotal 10000: discount 1500, total 8500.
- Toppings and quantities contribute to the subtotal before the discount.
- Quote and placed-order totals agree; old discount modes still work.
- Both receipt purposes render valid parseable JSON, including quote and
  newline characters in user-provided text.
- Unsupported formats are rejected, and another session cannot read the
  receipt. Existing session authorization must not be bypassed.
- One renderer works with both receipt abstractions through their common
  contract. Existing text and HTML receipt tests still pass.

Scope changes primarily to strategy and bridge plus the smallest existing
API/registry integration points. Do not change Observer or Decorator, the
public menu prices, CSS, or other students' tests. Do not introduce a new
top-level production package or another entry-point class. If the current
API differs from this prompt, reconcile with it instead of replacing it.

Create docs/contributions/AKMARAL.md explaining the classes I changed,
Strategy versus Bridge, the extension design, test commands/results and
the commit hashes once known. Do not claim work that did not happen.
Run the relevant tests and the complete Maven verification command in the
README. Check the diff for secrets and accidental generated files.

Before committing, inspect git user.name/user.email and the authenticated
GitHub account. Use my own existing identity. If they belong to someone
else, stop that commit step and ask me to configure my own account; never
invent my email, impersonate another student or modify global Git identity.
Make separate meaningful commits for Strategy and Bridge, then push my
branch. Open a pull request explaining behavior, validation and limitations.
Do not merge or squash the PR yourself. Return the branch, commit hashes,
PR URL and a short defense explanation I can rehearse.
```

## Completed task: Mukhammad - Observer and Decorator

Original task prompt, retained for the project record:

```text
I am Mukhammad, a member of the Restaurant Ordering System final project team.
Repository: https://github.com/ANAYSSA/Final-Project-SDP
My assigned patterns are Observer and Decorator.

Inspect the latest main branch, README, docs/REQUIREMENTS.md, current tests,
topping registration, status event model and observer lifecycle before
making changes. Work on a new branch named
feature/mukhammad-observer-decorator. Do not overwrite uncommitted work.

Implement two meaningful extensions:

1. Decorator: add HerbsDecorator, adding 150 KZT per decorated unit and a
   clear Herbs label while preserving the wrapped meal's identity and
   category. Add HERBS to the existing ToppingType metadata enum and
   register the decorator with id=herbs so GET /api/menu lists it and
   quote/order flows accept it. Keep its name and price in that one source. Permit it on pizza and pasta; retain
   the existing prohibition for drinks. It must compose with cheese,
   mushrooms and olives through the existing component contract. Reuse
   the project rules for repeated topping IDs and invalid topping values,
   and test those rules. Do not implement the price addition in the web
   controller or duplicate it in JavaScript.

2. Observer: implement an OperationsDashboardObserver subscriber that
   records one operations event for each successfully published order
   transition (including PLACED if that is the existing subject contract).
   Its message should contain only the order's public reference, new
   status and aggregate item quantity, with audience=OPERATIONS or the
   project's equivalent audience representation. Do not include customer
   names, table numbers, notes, session identifiers, tokens or full
   receipts. StatusChange currently carries orderId/status/time; pass the
   immutable aggregate item quantity into each per-order subscriber through
   its constructor, instead of giving the subscriber the whole Order.
   Keep this dashboard projection inside the existing session-
   scoped order/event view; do not expose a global endpoint or cross-session
   order list. An in-memory event projection is enough; no external service
   or logging provider is needed. Register it through the Observer contract
   with the existing subject. The subject must not know its concrete type.
   Preserve existing kitchen/customer notifications and valid transitions.

Add meaningful tests for:
- Margherita 2000 + herbs 150 = 2150; quantity 2 gives 4300.
- Herbs + cheese (400) produces 2550 per Margherita, in either wrapping
  order, with both labels and the original category preserved.
- Quote and placed-order totals agree, including existing discounts.
- Herbs is rejected for drinks and invalid topping input follows the
  existing API rules.
- Each successful transition produces exactly one operations event for
  that observer. A rejected transition produces no new event.
- Repeating the same idempotent order-creation request does not create a
  second order or duplicate notification.
- Unsubscribing the operations observer stops its future notifications
  without stopping kitchen/customer subscribers, if the existing subject
  supports unsubscribe; add a small contract-respecting implementation
  if necessary and test it.
- Event content contains no customer name or private note; another session
  cannot see the order or its events.

Scope changes primarily to decorator and observer plus the smallest
existing topping/observer registration points. Do not modify Strategy,
Bridge, their new modes, public base menu prices, CSS, or other students'
tests. Do not introduce a seventh top-level production package or a second
entry point. If current classes/API differ, adapt to the real design.

Create docs/contributions/MUKHAMMAD.md describing actual classes changed,
Decorator versus inheritance, Observer versus polling, subscription
lifecycle, privacy of events, test commands/results and commit hashes once
known. Do not claim work that did not happen. Run relevant tests and the
full Maven verification command in README. Check the diff for secrets and
generated files.

Before committing, inspect git user.name/user.email and the authenticated
GitHub account. Use my own existing identity. If they belong to someone
else, stop that commit step and ask me to configure my own account; never
invent my email, impersonate another student or modify global Git identity.
Make separate meaningful commits for Decorator and Observer, then push my
branch and open a PR with behavior and validation details. Do not merge or
squash it yourself. Return the branch, commit hashes, PR URL and a short
defense explanation I can rehearse.
```

## Final team check

The implementation tasks are merged. Before submission, use the final tested revision and matching report/source ZIP. Inspect `git log --format='%h %an <%ae> %s'` and the actual diffs when presenting individual work. Each member should explain their two pattern implementations, tests and interactions with the application.

Every student must submit the PDF report, source ZIP and GitHub link personally by the course deadline. The defense and individual submissions remain student responsibilities.
