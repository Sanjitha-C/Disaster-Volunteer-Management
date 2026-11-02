# DVC Initial Entity Relationship Diagram

This ER diagram reflects the Stage 2 JPA entities. The access-control and business workflows that use these records are planned for later stages.

```mermaid
erDiagram
  USER_ACCOUNTS ||--o| VOLUNTEER_PROFILES : "has profile"
  USER_ACCOUNTS ||--o{ EMERGENCY_REQUESTS : "submits"
  USER_ACCOUNTS ||--o{ NOTIFICATIONS : "receives"
  VOLUNTEER_PROFILES ||--o{ VOLUNTEER_SKILLS : "has"
  SKILLS ||--o{ VOLUNTEER_SKILLS : "assigned to volunteers"
  VOLUNTEER_PROFILES ||--o{ AVAILABILITY_SLOTS : "sets"
  EMERGENCY_REQUESTS ||--o{ EMERGENCY_REQUEST_SKILLS : "requires"
  SKILLS ||--o{ EMERGENCY_REQUEST_SKILLS : "required by requests"
  EMERGENCY_REQUESTS ||--o{ TASK_ASSIGNMENTS : "creates"
  VOLUNTEER_PROFILES ||--o{ TASK_ASSIGNMENTS : "receives"
  TASK_ASSIGNMENTS ||--o| VOLUNTEER_FEEDBACK : "may receive"

  USER_ACCOUNTS {
    bigint id PK
    varchar full_name
    varchar email UK
    varchar phone
    varchar address
    varchar password_hash
    varchar role
    varchar account_status
  }
  VOLUNTEER_PROFILES {
    bigint id PK
    bigint user_id FK
    varchar address
    varchar availability
  }
  SKILLS {
    bigint id PK
    varchar name UK
  }
  VOLUNTEER_SKILLS {
    bigint id PK
    bigint volunteer_id FK
    bigint skill_id FK
  }
  AVAILABILITY_SLOTS {
    bigint id PK
    bigint volunteer_id FK
    datetime starts_at
    datetime ends_at
  }
  EMERGENCY_REQUESTS {
    bigint id PK
    bigint requester_id FK
    varchar title
    varchar location
    varchar urgency
    varchar status
  }
  EMERGENCY_REQUEST_SKILLS {
    bigint id PK
    bigint request_id FK
    bigint skill_id FK
  }
  TASK_ASSIGNMENTS {
    bigint id PK
    bigint request_id FK
    bigint volunteer_id FK
    varchar status
  }
  RESOURCES {
    bigint id PK
    varchar name
    int quantity
    varchar unit
    varchar status
  }
  NOTIFICATIONS {
    bigint id PK
    bigint recipient_id FK
    varchar title
    boolean is_read
  }
  VOLUNTEER_FEEDBACK {
    bigint id PK
    bigint assignment_id FK
    int rating
    varchar comments
  }
```

Foreign keys are represented by JPA relationships. Unique constraints prevent duplicate account emails, duplicate skill names, duplicate volunteer/request skill links, duplicate request-volunteer assignments, and multiple feedback rows for one assignment. Query indexes cover common user role/status, request status/urgency, assignment status, availability lookup, resource status, and notification-recipient lookup.
