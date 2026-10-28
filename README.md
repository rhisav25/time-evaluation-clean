# Configurable Time Evaluation Engine

A Java 21 Spring Boot application that evaluates attendance intervals and produces named wages as durations in minutes (not money).

Which wages exist, how they are calculated and how they depend on each other is defined **in `application.yml`**, not in Java code.

## Requirements

- Java 21
- Nothing else: the Maven Wrapper (`mvnw`, `mvnw.cmd`) is included and downloads Maven on first run

## Run

```bash
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows
```

The application starts on `http://localhost:8080`.

## Run tests

```bash
./mvnw clean test
```

## API

`POST /api/evaluations` with `periodStart`, `periodEnd`, `attendanceIntervals` and `dailyInformation`
(see `src/main/resources/examples/`). The response contains `dailyResults` and `weeklyResults`, each a list of named wages.

```powershell
$response = Invoke-RestMethod `
  -Uri "http://localhost:8080/api/evaluations" -Method Post -ContentType "application/json" `
  -InFile "src/main/resources/examples/evaluation-request-example-b.json"
$response | ConvertTo-Json -Depth 10
```

Weekly rules need a complete Monday-Sunday period. If no weekly rule is configured, any period is accepted.

## Example inputs and configurations

`src/main/resources/examples/`:

- `evaluation-request-example-a.json` - 42 h week: 4 h daily overtime, 0 h weekly overtime remaining
- `evaluation-request-example-b.json` - 46 h week: 2 h daily overtime, 4 h weekly overtime remaining
- `evaluation-request-public-holiday.json` - a Sunday that is also a public holiday: only the holiday wage

## Configuring rules

Rules are a list in `application.yml`, evaluated **top to bottom**. A rule may only use rules defined above it.

```yaml
evaluation:
  rules:
    - name: attendance                 # name of the wage
      type: ATTENDANCE_SUM
      scope: DAY

    - name: daily-overtime
      type: EXCESS_OVER_EXPECTED
      scope: DAY
      input: attendance                # uses the rule above
      days: [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]

    - name: week-attendance
      type: SUM_OVER_DAYS
      scope: WEEK
      input: attendance
      visible: false                   # intermediate value, not in the response
```

| Type | Scope | What it does | Fields |
|---|---|---|---|
| `ATTENDANCE_SUM` | DAY | sum of the day's intervals, gaps excluded | - |
| `EXCESS_OVER_EXPECTED` | DAY | `max(0, input - expected duration of the day)` | `input`, optional `days` |
| `OUTSIDE_WINDOW` | DAY | attendance outside the expected working window | - |
| `DAY_FILTER` | DAY | passes `input` through only if the day matches, else 0 | `input`, optional `days`, optional `public-holiday` |
| `SUM_OVER_DAYS` | WEEK | sums a DAY rule over the week | `input` (a DAY rule) |
| `EXCESS_OVER_THRESHOLD` | any | `max(0, input - threshold-minutes)` | `input`, `threshold-minutes` |
| `SUBTRACT_FLOOR_ZERO` | any | `max(0, input - subtract)` | `input`, `subtract` |

Common fields: `name` (unique), `type`, `scope`, `visible` (default `true`).

The configuration is validated at startup (unknown references, duplicate names, wrong scope, missing fields). The application refuses to start with a message naming the faulty rule.

### How the scenarios are configured

1. **Attendance** - `ATTENDANCE_SUM`.
2. **Daily overtime** - `EXCESS_OVER_EXPECTED` with `days`.
3. **Outside window** - `OUTSIDE_WINDOW`.
4. **Sunday / holiday** - two `DAY_FILTER` rules: Sunday work has `days: [SUNDAY]` and `public-holiday: false`; holiday work has `public-holiday: true`. That is how the holiday wins.
5. **Reconciliation** - sum attendance and daily overtime over the week, `EXCESS_OVER_THRESHOLD` on the weekly attendance, then `SUBTRACT_FLOOR_ZERO` of the weekly daily overtime. Daily overtime is untouched.

Adding a **new rule type** needs one small class implementing `Rule`, an enum value, and one entry in `RuleFactory`.

## Design and assumptions

See `docs/design.md`.
