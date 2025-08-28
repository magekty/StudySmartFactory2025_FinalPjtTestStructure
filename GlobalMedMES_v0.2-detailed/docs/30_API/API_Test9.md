# swagger
- http://localhost:8080/swagger-ui/index.html

# 1 로그인
- http
POST /auth/login
Body
- json
{ "username":"op", "gmmes1121":"<시드의 실 해시에 대응하는 비번>" }
Expect: 200, body.token 존재
Postman: Tests에 token 변수 저장
- js
const r = pm.response.json();
pm.collectionVariables.set('token', r.token);

# 2 메뉴(미로그인)
- http
GET /menus/my → 401 기대

# 3 메뉴(로그인)
Headers: Authorization: Bearer {{token}}
GET /menus/my → 200, menus 배열 확인

# 4 작업지시 생성(P)
- http
POST /work-orders (Authorization 포함)
Body
- json
{
  "workOrderNumber": "WO-API-0001",
  "itemId": "I-0001",
  "processId": "P-0001",
  "equipmentId": "E-0001",
  "orderQty": 100
}
Expect: 201, status=P, workOrderId 저장

# 5 P→R
- http
PUT {{baseUrl}}/work-orders/{{woId}}/status

Headers: Authorization: Bearer {{token}}

Body
- json
{ "toStatus": "R" }
Expect: 200, status=R

# 6 R→C
- http
PUT /work-orders/{{woId}}/status

Authorization: Bearer {{token}}

- Content-Type: application/json
{
  "toStatus": "C"
}
Expect: 200, status=C

# 7 설비 상태 RUN
(컨트롤러가 있을 때)
- http
POST /equip-status

Authorization: Bearer {{token}}

Content-Type: application/json
{
  "equipmentId": "E-0001",
  "statusCode": "RUN",
  "startTimeUtc": "2025-08-10T09:00:00Z"
}
Expect: 201/200

# 8 실적 등록
- http
POST /performances

Authorization: Bearer {{token}}

Content-Type: application/json
{
  "workOrderId": "{{woId}}",
  "itemId": "I-0001",
  "processId": "P-0001",
  "equipmentId": "E-0001",
  "producedQty": 100,
  "defectQty": 5,
  "startTime": "2025-08-10T09:00:00Z",
  "endTime": "2025-08-10T09:30:00Z"
}
Expect: 201, body.goodQty=95

# 9 KPI 실제치
(컨트롤러가 있을 때)
- http
GET /kpi/actuals?kpiDate=2025-08-10&equipmentId=E-0001

Authorization: Bearer {{token}}

Expect: 200

# API 계약 최소 고정
/menus/my 응답(고정 스키마)
json


{
  "user": "0000-...-OP",
  "menus": [
    {
      "code": "WO",
      "name": "작업지시",
      "path": "/work-orders",
      "perms": { "read": true, "write": true, "exec": false },
      "children": []
    }
  ]
}
# 페이지네이션 응답 껍데기(공통)
json


{
  "content": [ /* 항목들 */ ],
  "page": 0,
  "size": 20,
  "totalElements": 123,
  "totalPages": 7,
  "sort": "createdAt,desc"
}