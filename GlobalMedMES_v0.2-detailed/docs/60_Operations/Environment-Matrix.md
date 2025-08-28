# Environment Matrix

| 항목           | dev                         | stg   | prod  |
|----------------|-----------------------------|-------|-------|
| DB URL         | TODO                        | TODO  | TODO  |
| JWT Secret     | env(APP_JWT_SECRET)         | env   | env   |
| JWT TTL(min)   | 480                         | TODO  | TODO  |
| Server Port    | 8080                        | 8080  | 8080  |
| CORS           | http://localhost:5173       | TODO  | TODO  |
| FE API Base    | .env.local VITE_API_BASE    | TODO  | TODO  |
| Docker         | yes                         | yes   | yes   |
| Log Level      | debug                       | info  | warn  |

- 주입 키: app.security.jwt.secret, app.security.jwt.ttl-minutes