### ** react 명명 규칙**
### **1. 파일 및 컴포넌트 명명 규칙**

- **컴포넌트 파일명:** 컴포넌트 이름과 동일하게 **`PascalCase`*를 사용합니다.
    - **예시:** `ProductList.tsx`, `SidebarMenu.tsx`
- **컴포넌트 이름:** 파일명과 동일한 **`PascalCase`*를 사용합니다.
    - **예시:** `const ProductList = () => { ... }`
- **컴포넌트 내부 함수/변수:** **`camelCase`*를 사용합니다.
    - **예시:** `const handleButtonClick = () => { ... }`
- **컴포넌트 props:** **`camelCase`*를 사용하며, Boolean props는 `is`, `has`, `can` 접두사를 붙여 명확하게 표현합니다.
    - **예시:** `<ProductItem isSelected={true} hasStock={false} canEdit={user.role === 'admin'} />`
- **훅(Hook) 파일명:** `use` 접두사를 붙여 **`camelCase`*를 사용합니다.
    - **예시:** `useInventoryData.ts`, `useAuth.ts`
    - **주의사항:** 커스텀 훅은 항상 `use`로 시작해야 합니다.

---

### **2. 컴포넌트 구조 및 스타일링**

- **기능별 분리:** 하나의 컴포넌트가 너무 많은 역할을 하지 않도록 **책임(Responsibility)**을 분리합니다.
    - **`Container Component`** (데이터 로직 담당)와 **`Presentational Component`** (UI 렌더링 담당)를 분리하여 재사용성과 유지보수성을 높입니다.
    - **예시:** `ProductListContainer.tsx`에서 데이터를 가져오고, `ProductListComponent.tsx`에서 데이터를 받아 화면을 그립니다.
- **폴더 구조:** `src/features/{기능명}/` 폴더 아래에 `components`, `hooks`, `types` 등으로 파일을 분류하여 모듈화합니다.
- **스타일링:** CSS-in-JS (예: Styled-Components) 또는 CSS Module을 주로 사용합니다.
    - **Styled-Components:** 컴포넌트 파일 내에서 UI와 스타일을 함께 관리할 수 있어 편리합니다.
    - **CSS Module:** `.module.css` 파일을 사용해 클래스 이름 충돌을 방지하며, 컴포넌트별로 스코프(Scope)를 제한합니다.

---

### **3. 상태 관리 및 Props**

- **디스트럭처링 (Destructuring):** Props를 객체로 받아 사용하기 전 **디스트럭처링**하여 가독성을 높입니다.
    - **예시:** `const ProductCard = ({ product, onSelect, isSelected }) => { ... }`
- **Props 타입:** TypeScript를 사용하여 Props의 타입을 명확하게 정의하고, 이를 주석으로 대체하는 행위를 지양합니다.
    - **예시:** `interface ProductCardProps { product: ProductType; onSelect: () => void; isSelected: boolean; }`
- **불변성(Immutability):** 상태를 업데이트할 때 직접 수정하지 않고, **새로운 객체나 배열을 생성**하여 업데이트합니다. 이는 React의 렌더링 최적화와 상태 추적에 필수적입니다.
    - **잘못된 예시:** `product.name = 'New Name';`
    - **올바른 예시:** `setProduct({ ...product, name: 'New Name' });`
- **Prop-drilling 방지:** Props가 여러 컴포넌트를 거쳐 전달되어야 할 경우 (Prop-drilling), **Context API** 또는 **상태 관리 라이브러리(Recoil, Zustand)** 사용을 고려합니다.

---

### **4. 코드 품질 및 성능**

- **React Fragment:** 불필요한 DOM 노드 생성을 막기 위해 `<div>` 대신 `<React.Fragment>` 또는 `</>`를 사용합니다.
- **Key Prop:** 리스트 렌더링 시 반드시 **고유한 값(Unique ID)**을 `key`로 사용합니다. `index`는 리스트 순서가 변경되지 않는 경우에만 사용합니다.
- **useEffect Dependency Array:** `useEffect`의 의존성 배열(`[]`)을 항상 명확하게 지정하여 **불필요한 리렌더링을 막습니다.** 
    의존성이 없는 경우 빈 배열(`[]`)을 사용하고, `state`나 `prop`이 포함되면 훅이 실행되어야 할 조건을 명확히 합니다.
- **로딩 및 에러 처리:** 데이터 요청 시 `isLoading`, `error` 상태를 명확하게 관리하고, UI에 반영하여 사용자 경험을 개선합니다.
/////////////////////////////////////////////////////////////////
### ** Java 명명 규칙**
- **클래스/인터페이스:** **`PascalCase`*를 사용하며, 역할에 따라 접미사를 붙여 명확히 구분합니다.
    - **예시:** `ItemService`, `ItemRepository`, `ItemController`
- **메서드/변수:** **`camelCase`*를 사용합니다.
    - **예시:** `private int inventoryCount;`, `public void createItem() { ... }`
- **상수:** 모든 문자를 대문자로 하고, 단어 사이에 언더스코어(`_`)를 사용합니다.
    - **예시:** `public static final String DEFAULT_STATUS = "PLAN";`
- **패키지:** 소문자로만 구성하고, 단어 사이에 마침표(`.`)를 사용합니다.
- 패키지 상세 컨벤션
    
    ### **1. 기본 원칙**
    - **소문자 사용:** 모든 패키지 이름은 소문자로만 구성합니다.
    - **단어 구분:** 단어 사이에 언더스코어(`_`)나 하이픈() 대신 마침표(`.`)를 사용하여 계층 구조를 나타냅니다.
    - **역순 도메인:** 패키지명의 시작은 회사의 도메인 이름을 역순으로 작성하는 것이 표준입니다.
        - **예시:** `com.mycompany.projectname`
    - **세부 계층:** `프로젝트명` 다음에는 **`도메인`*과 **`계층`*을 순서대로 명시하여 패키지 구조를 명확히 합니다.
    
    ### **2. 패키지 구조 (도메인 + 계층)**
    가장 보편적인 실무 패키지 구조는 **`com.도메인.프로젝트명.기능_도메인.계층`** 형식입니다. 
    이 구조를 통해 코드가 어떤 도메인에 속하고, 어떤 역할을 하는지 한눈에 파악할 수 있습니다.
    
    ### **예시: `com.mes.pjt.production.workorder.service`**
    
    - **`com.mes.pjt`**: 프로젝트의 최상위 루트 패키지입니다.
        - `com`: 최상위 도메인 (관례적으로 `com`, `org`, `net` 등 사용).
        - `mes`: 회사 또는 프로젝트명 (여기서는 `Manufacturing Execution System`).
        - `pjt`: 특정 프로젝트를 구분하기 위한 접미사 (선택 사항).
    - **`production`**: 상위 도메인으로, `생산 관리`와 관련된 모든 코드가 이 패키지 아래에 위치합니다.
    - **`workorder`**: 하위 도메인으로, `작업 지시`와 관련된 구체적인 기능들을 모아놓습니다.
    - **`service`**: 코드가 속한 **계층(Layer)**을 나타냅니다. 이 패키지에는 `비즈니스 로직`을 처리하는 Service 클래스들이 들어갑니다.
    
    ### **3. 계층별 패키지 명명 (상세)**
    프로젝트의 규모와 관계없이, 아래와 같은 계층별 패키지 명명 규칙을 적용하면 코드의 역할이 명확해집니다.
    - **`controller`**: API 엔드포인트를 정의하는 클래스.
        - `com.mes.pjt.item.controller`
        - `ItemController.java`
- **Boolean 변수:** 변수의 의미를 명확히 하는 접두사를 사용합니다.
    - **`is` + 동사:** 상태를 나타낼 때 사용합니다. (예: `isCompleted`, `isDeleted`, `isAvailable`)
    - **`has` + 명사:** 소유 여부를 나타낼 때 사용합니다. (예: `hasPermission`, `hasItems`, `hasChildren`)
    - **`can` + 동사:** 가능 여부를 나타낼 때 사용합니다. (예: `canEdit`, `canView`)

### **2. 코드 구조 및 가독성**
- **단일 책임 원칙 (SRP):** 하나의 클래스/메서드는 하나의 책임만 갖도록 설계합니다. 복잡한 로직은 Helper 클래스나 메서드로 분리합니다.
- **메서드 길이:** 한 메서드는 **10~15줄 이내**로 작성하도록 노력하며, 들여쓰기(Indentation) 깊이는 **최대 2단계**를 넘지 않도록 합니다.
- **JavaDoc:** 클래스, 메서드, 필드의 역할을 명확히 문서화하여 다른 개발자가 코드를 빠르게 이해하도록 돕습니다.
- **주석:** 코드의 동작 방식보다는 **'왜 이 코드가 필요한지'**에 대한 설명을 추가합니다. 
    특히 비즈니스 로직의 복잡한 부분이나 예외적인 처리에 대한 주석을 상세히 남깁니다.

### **3. 데이터 및 API 처리**
- **DTO (Data Transfer Object):** 계층 간(예: Controller -> Service) 데이터 전송 시 DTO를 사용합니다. 
    `Entity` 객체를 직접 반환하거나 요청으로 받지 않도록 합니다.
    - `CreateRequestDto`, `UpdateRequestDto`, `ResponseDto`와 같이 역할에 따라 이름을 명확히 지정합니다.
- **예외 처리:** 사용자에게 보여줄 에러 메시지를 포함한 **커스텀 예외(Custom Exception)**를 정의하여 사용하고, 
    Controller 단에서 `@ExceptionHandler`를 통해 일괄적으로 처리합니다.
- **RESTful API:** API URL은 명사를 사용하고, HTTP 메서드(GET, POST, PUT, DELETE)를 통해 동작을 정의합니다.
    - **예시:** `GET /api/v1/items/{itemId}` (단일 조회), `POST /api/v1/items` (등록)
- **UUID 사용:** 프로젝트의 ID처럼, `UUID`는 **`String` 타입**으로 관리하고, 
    DB에 저장하기 전 `toString()`으로 변환합니다. `AUTO_INCREMENT`는 사용하지 않습니다.

/////////////////////////////////////////////////////////////////
## 📦 전체 기술 스택

| 구분 | 항목 | 권장 버전 | 비고 |
| --- | --- | --- | --- |
| **Frontend** | Node.js | `18.17.1 LTS` | 안정성 + Tailwind 지원 |
|  | React | `18.2.x` | 최신 안정 버전 |
|  | Tailwind CSS | `3.4.x` | JIT 기반 |
|  | Vite | `5.4.19` | 빠른 개발 서버 |
|  | 패키지 매니저 | `pnpm` or `npm` | `pnpm` 권장 |
| **Backend** | Java | `17 LTS` | Spring Boot 3.x 호환 |
|  | Spring Boot | `3.2.x` | Java 17 이상 필요 |
|  | Build Tool | `Gradle 8.x` | Spring Initializr 선택 시 기본 |
| **Database** | MySQL | `8.0.x` | 최신 기능(윈도우 함수 등) 활용 |
| **Infra** | Docker | `20.10.x` 이상 | 개발/운영 환경 분리 가능 |
|  | GitHub | N/A | Git 버전 관리 |
|  | Notion | N/A | API 문서/회의록 관리 |
| **언어** | 기본 언어 | 한국어 | 한글 주석 및 한글 UI 사용 |

# ErrorCodes (표준 사전)
| 코드 | HTTP | 설명 |
|---|---|---|
| AUTH_REQUIRED | 401 | 인증 필요 |
| FORBIDDEN | 403 | 권한 부족 |
| VALIDATION_ERROR | 400 | 필드 검증 실패 |
| DUPLICATE_KEY | 409 | 고유키 충돌 |
| WO_STATUS_INVALID | 400 | 지시 상태 전이 불가 |
| CODE_INACTIVE | 400 | 비활성 코드 참조 |
| TIME_ORDER_INVALID | 400 | 시간 역전(또는 동일 시각 금지) |
| SERVER_ERROR | 500 | 내부 오류 |

//////////////////////////////////////////////////////////
💡 GlobalMed MES API 프로젝트 개요
항목	내용
프로젝트명	GlobalMed MES API
버전	v1
목표	로그인 → 작업 지시 → 생산 실행(RUN) → 실적 등록 → KPI 측정의 핵심 제조 실행 시스템(MES) 흐름을 지원하는 API 구축.
기본 URL	http://localhost:8080
인증 방식	Bearer Token (JWT) 기반 보안 적용.

2. API 핵심 흐름 (Core Business Flow)
이 API는 제조 현장의 주요 활동을 디지털화하는 다음의 4가지 핵심 모듈로 구성되어 있습니다.

핵심 모듈	주요 기능 (엔드포인트)	비즈니스 가치
1. 작업 지시 (Work Order)	지시 생성 (POST /work-orders), 상태 변경 (PUT /work-orders/{id}/status), 목록 조회 (GET /work-orders)	생산 계획을 현장에 전달하고, 지시의 라이프사이클 관리
2. 생산 실적 (Performance)	실적 등록 (POST /performances), 실적 조회 (GET /performances)	생산 결과 (생산량, 불량량)를 기록하여 생산성 분석의 기초 데이터 확보
3. 설비 관리 (CMMS / Downtime)	설비 가동 상태 기록, 계획된 비가동 등록, 고장 로그 관리	설비의 가용성을 높이고 유지보수 활동을 체계화
4. 현황 및 KPI	설비 상태 조회, KPI 데이터 목록 조회 (GET /kpi/datalist), 실시간 KPI 계산 (GET /kpi/actuals)	생산 현황 및 핵심 성과 지표(OEE, 생산성 등)를 실시간으로 모니터링

3. 모듈별 주요 엔드포인트 상세 목록
3.1. 🏭 작업 지시 (Work Order) 관리
API 기능	HTTP 메서드	엔드포인트	요청 데이터 (필수)
지시 생성	POST	/work-orders	workOrderNumber, itemId, processId, equipmentId, orderQty
지시 상태 변경	PUT	/work-orders/{id}/status	id(Path), toStatus(Body)
지시 상세 조회	GET	/work-orders/{id}	id(Path)
지시 목록 조회	GET	/work-orders	page, size (Query) + 다양한 검색 필터

3.2. 📈 생산 실적 및 KPI
API 기능	HTTP 메서드	엔드포인트	요청 데이터 (필수)
실적 등록	POST	/performances	workOrderId, itemId, producedQty, defectQty, startTime, endTime
실적 목록 조회	GET	/performances	page, size (Query) + 기간, 지시 번호 등 검색 필터
KPI 데이터 조회	GET	/kpi/datalist	페이징 및 kpiDate, equipmentId, processId 등 검색 필터
실시간 KPI (Actuals)	GET	/kpi/actuals	kpiDate, equipmentId (Query)

3.3. 🛠️ 설비/유지보수 (CMMS & Downtime)
이 섹션은 CMMS (Computerized Maintenance Management System) 기능을 지원합니다.

API 기능	HTTP 메서드	엔드포인트	주요 역할
설비 상태 기록	POST	/equip-status	설비의 가동/비가동 상태 변화를 기록
계획 비가동 등록	POST	/planned-downtime	설비의 예정된 정지 시간 (예: 정기 보수) 등록
고장 로그 등록	POST	/cmms/fault-logs	현장에서 발생한 고장(symptom, occurredAt) 기록
CMMS 작업지시 생성	POST	/cmms/work-orders	유지보수 작업 지시 생성 (title, equipmentId, priorityCodeId)
CMMS 작업 실행/완료	POST	/cmms/work-orders/{id}/start / /complete	유지보수 작업의 시작 및 완료 처리
PM 계획 등록/완료	POST	/cmms/pm-plans / /{id}/done	예방 보전(PM) 계획 등록 및 완료 처리

3.4. 👤 사용자 및 교대조 관리
API 기능	HTTP 메서드	엔드포인트	주요 역할
로그인	POST	/auth/login	시스템 접근을 위한 인증 처리
근태 기록	POST	/user/attendance	사용자 근태 상태 기록 (status)
교대조 캘린더 생성	POST	/shifts/calendars/generate	특정 일자와 설비/작업장에 대한 교대조 캘린더 생성
작업자 배치	POST	/shifts/{calendarId}/assign	특정 교대조 캘린더에 작업자 배정
메뉴 조회	GET	/menus/my	로그인 사용자의 권한별 메뉴 목록 제공
직원 목록 조회	GET	/employees	직원 정보 목록 조회

4. 데이터 모델의 연관성 (Schema 간의 관계)
API의 견고함은 데이터 모델에서 나옵니다. 주요 엔드포인트는 다음과 같은 핵심 데이터 구조(components/schemas)를 활용합니다.

핵심 데이터 모델	연관된 주요 엔드포인트	특징
WorkOrder (CreateReq, ListDto)	/work-orders	작업 지시의 계획 정보 (품목, 공정, 수량) 정의.
Performance (Req, ListDto)	/performances	생산 실적 기록 (생산량, 불량량, 시간) 정의.
WorkOrderRes (CMMS)	/cmms/work-orders	유지보수 작업 지시의 상태 및 상세 정보 정의.
ShiftAssignment	/shifts/assignments	작업자, 설비, 교대조, 시간 정보의 연결고리.
KpiDataListDto	/kpi/datalist	OEE, 생산성, 수율 등 핵심 성과 지표의 결과 구조.