# Team implementation tasks

These prompts are intended for each student's own AI-assisted development session. Each student must inspect, understand, implement, test and explain their changes. The existing baseline was committed under Anuar's identity; it does not establish the other students' individual contributions. Do not fabricate authorship or replace another person's Git identity.

The tasks below are substantive extensions to each student's assigned pair of patterns. They provide reviewable evidence of implementation, but the team must still demonstrate to the lecturer that each student has personally implemented and understands two patterns. Keep the final contribution statement factual.

## Akmaral — Strategy and Bridge

Copy this prompt into your development agent:

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

## Mukhammad — Observer and Decorator

Copy this prompt into your development agent:

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

## Integration and author history

Anuar reviews both pull requests, runs the complete verification suite on the combined result, resolves overlapping registration changes and updates the final report. Use a merge commit or another merge method that preserves the original student authors. Avoid replacing the students' commits with a single Anuar-authored commit. Never force-push shared `main` to manufacture a contribution history.

After merging, inspect `git log --format='%h %an <%ae> %s'` and `git shortlog -sne --all`, then inspect the actual changed files. A visible contribution chart or a commit count alone is not sufficient evidence of implementing two patterns. Each teammate must be able to explain their assigned contracts, concrete implementations, tests and runtime behavior.

The tasks deliberately avoid frontend redesign. The frontend should discover toppings and discounts from the menu API. If it does not, make the smallest integration change after coordinating the two PRs. JSON receipts can be demonstrated with the existing receipt URL and `format=json` without a separate page.
