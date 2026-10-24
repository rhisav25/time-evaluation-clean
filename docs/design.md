# Design Notes

## Architecture

- `EvaluationController` exposes `POST /api/evaluations`; `GlobalExceptionHandler` maps invalid input to HTTP 400.
- `EvaluationService` validates the request and evaluates the configured rules. It contains **no wage names and no rule-specific logic**.
- `EvaluationProperties` binds the rule list from `application.yml` into `RuleDefinition` records.
- `RuleFactory` turns definitions into `Rule` objects and validates the configuration once at startup.
- `Rule` implementations (one small class per `RuleType`) do one calculation each. They reuse `AttendanceCalculator` and `OvertimeCalculator`, which hold the plain arithmetic.
- `EvaluationContext` carries what a rule may read: the day's data, or the daily results of the week, plus the values of rules evaluated earlier in the same scope.
- Java records represent request data and the results.

## How evaluation works

1. Validate the request (period, one entry per day, intervals inside the period, no overlaps, window consistency).
2. For every day, run all `DAY` rules in configured order. Each result is stored under the rule's name.
3. If weekly rules exist, run all `WEEK` rules in configured order. `SUM_OVER_DAYS` reads the stored daily values, so weekly rules never depend on the response format.
4. Only rules with `visible: true` appear in the response.

## Calculation rules

- **Attendance:** sum of intervals, gaps excluded.
- **Daily overtime:** `max(0, attendance - expected duration)` on the configured weekdays.
- **Outside window:** attendance before the window start or after the window end.
- **Sunday / public-holiday work:** two `DAY_FILTER` rules; Sunday work requires `public-holiday: false`, so the holiday wage wins.
- **Weekly overtime:** `max(0, weekly attendance - threshold)`, then `max(0, that - weekly daily overtime)`. Daily overtime stays unchanged.

All durations are integer minutes.

## Assumptions and validation

- One employee; minute precision; intervals do not cross midnight.
- Weekly rules need a complete Monday-Sunday period (one week per request).
- Each date in the period has exactly one daily-information entry.
- Public-holiday status is supplied in the request; the weekday is derived from the date.
- Weekly overtime uses a configured threshold, not the sum of the daily expected durations.

## Trade-offs and limitations

- **Building blocks, not a rule language.** Users combine seven rule types freely, but a calculation none of them can express needs a new class. A general expression language would be more flexible but harder to validate and secure.
- **Order-based dependencies.** A rule may only use rules above it. This keeps the engine trivial and makes circular references impossible, at the cost of the user having to order the list sensibly.
- **Monthly aggregation is not implemented.** No scenario requires it. It could be added as `Scope.MONTH` with a `SUM_OVER_DAYS`-like rule that sums per calendar month.
- Only one week per request is evaluated (no multi-week periods).
- Rules create their own calculator instances rather than receiving Spring beans; the calculators are stateless.
- No database or GUI; no overnight shifts.
