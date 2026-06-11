# API Integration Contract

## Environment

V3 reads the backend base URL from `BuildConfig.API_BASE_URL`.

- `dev`: local Strapi-compatible API, cleartext allowed.
- `qa`: QA Strapi-compatible API, cleartext allowed until a QA HTTPS domain is available.
- `prod`: HTTPS API placeholder, cleartext disabled.

## Authentication

### Login

`POST auth/local`

Request:

```json
{
  "identifier": "username-or-email",
  "password": "password"
}
```

Response:

```json
{
  "jwt": "token",
  "user": {
    "id": 1,
    "username": "demo",
    "email": "demo@example.com"
  }
}
```

### Register

`POST auth/local/register`

Request:

```json
{
  "username": "demo",
  "email": "demo@example.com",
  "password": "password"
}
```

Response uses the same shape as login.

## Orders

Authenticated requests should include:

```http
Authorization: Bearer <jwt>
```

The app injects this header through the shared OkHttp client.

### List Orders

`GET orders?sort=createdAt:desc`

Expected response:

```json
{
  "data": [
    {
      "id": 1,
      "documentId": "abc123",
      "orderNumber": "ORD001",
      "goodsInfo": "Sample goods",
      "status": "待发货",
      "createTime": "2026-06-10T00:00:00.000Z",
      "createdAt": "2026-06-10T00:00:00.000Z"
    }
  ],
  "meta": {}
}
```

### Create Order

`POST orders`

Request:

```json
{
  "data": {
    "orderNumber": "ORD001",
    "goodsInfo": "Sample goods",
    "status": "待发货",
    "createTime": "2026-06-10T00:00:00.000Z"
  }
}
```

### Delete Order

`DELETE orders/{documentId}`

Strapi 5 order deletion uses the order `documentId`, not the numeric `id`. Android should read `documentId` from the order list/create response and pass it as the path parameter.

Success response:

```json
{
  "data": {
    "id": 1,
    "documentId": "abc123",
    "orderNumber": "ORD001",
    "goodsInfo": "Sample goods",
    "status": "待发货",
    "createTime": "2026-06-10T00:00:00.000Z",
    "createdAt": "2026-06-10T00:00:00.000Z"
  },
  "meta": {}
}
```

Common failures:

- `401`: token missing or expired.
- `403`: authenticated user does not have permission to delete the order.
- `404`: order does not exist or has already been deleted.

## AI Customer Service

### Streaming Answer

`POST deepseek/ask-trade-question-stream`

Request:

```json
{
  "question": "如何计算报关费用？",
  "history": [
    { "role": "user", "content": "previous question" },
    { "role": "assistant", "content": "previous answer" }
  ]
}
```

The app expects Server-Sent Events style chunks with `data: ...` lines and supports these common content shapes:

```json
{ "content": "text" }
```

```json
{ "delta": { "content": "text" } }
```

```json
{ "choices": [{ "delta": { "content": "text" } }] }
```

Stream completion may be signaled with:

```text
data: [DONE]
```
