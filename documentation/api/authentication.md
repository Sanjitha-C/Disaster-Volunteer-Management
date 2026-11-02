# Authentication API (Stage 3)

The API uses server-side sessions. Successful login returns an `HttpOnly` session cookie; the backend does not return a bearer token. Password hashes are never included in API response DTOs.

## Local demo accounts

The `demo` profile creates these accounts if absent. It does not overwrite existing users or reset their passwords.

| Role | Email | Default development password |
|---|---|---|
| ADMIN | `admin@gmail.com` | `Admin@12345` |
| VOLUNTEER | `volunteer@gmail.com` | `Volunteer@12345` |
| USER | `user@gmail.com` | `User@12345` |

Use the `DVC_DEMO_*` environment variables to configure seed credentials. The default profile is `demo` for local development; set `spring.profiles.active=prod` in a deployed environment to disable demo seeding.

## Routes

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/auth/csrf` | Public | Initializes the CSRF cookie; returns the expected header and cookie names. |
| POST | `/api/auth/register` | Public + CSRF | Creates an active VOLUNTEER account and an unavailable volunteer profile. The caller cannot select a role. |
| POST | `/api/auth/register/requester` | Public + CSRF | Creates an active USER account for a community member who needs to submit and track disaster requests. The caller cannot select a role. |
| POST | `/api/auth/login` | Public + CSRF | Authenticates email/password and stores authentication in the session. |
| POST | `/api/auth/logout` | Authenticated + CSRF | Invalidates the current session. |
| GET | `/api/auth/me` | Authenticated | Returns the current account identity, role, and status. |

Example volunteer registration body:

```json
{
  "fullName": "Asha Volunteer",
  "email": "asha@example.org",
  "phone": "+91 9000000004",
  "password": "ChooseA-Random-Password",
  "address": "Coimbatore, Tamil Nadu"
}
```

Requester registration uses the same fields and validation. Open `/register/requester` in the frontend, or POST to `/api/auth/register/requester`. The account is assigned the `USER` role by the server; after registration the user can sign in and open **Submit emergency request**, **My requests**, and **Request tracking**. The existing `/register` page continues to create volunteer accounts.

Community requesters can view and update their own profile with `GET` and `PUT /api/user/me/profile`. Administrators can use `GET` and `PUT /api/admin/me/profile` for their own profile. Both profile forms allow editing name, sign-in email, phone, and address. Email changes are normalized and rejected if another account already uses that address; the active session is updated to the new email. Volunteer profile editing remains at `/api/volunteer/me/profile`.

Validation errors return HTTP 400 with field-level messages. Duplicate email registration returns HTTP 409. Invalid credentials return HTTP 401 with a generic message. Restricted role paths return HTTP 403.

## Browser integration

The frontend origin `http://localhost:5173` is allowed by CORS with credentials. Before a state-changing request, call `/api/auth/csrf` with credentials so the `XSRF-TOKEN` cookie is set, then send that cookie's value in `X-XSRF-TOKEN`. Keep cookies enabled on API requests. The session cookie is HttpOnly and SameSite Lax.

## Role access

`/api/admin/**`, `/api/volunteer/**`, and `/api/user/**` require their corresponding role on the server. All other API paths require authentication unless explicitly listed as public in `SecurityConfig`.
