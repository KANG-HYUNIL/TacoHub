# TacoHub API 문서

## 목차
1. [사용자 계정 관리 API](#사용자-계정-관리-api)
2. [이메일 인증 API](#이메일-인증-api)
3. [워크스페이스 관리 API](#워크스페이스-관리-api)

---

## 사용자 계정 관리 API

### 1. 회원가입

**URL:** `/account/postSignup/{authCode}/{purpose}`

**Method:** `POST`

**Path Variable:**
- `authCode`: 이메일로 받은 인증 코드
- `purpose`: 인증 목적 (예: "signup")

**Request Body:**
```
emailId: String (이메일 주소)
password: String (비밀번호)
name: String (사용자 이름)
role: String (사용자 권한, "ROLE_USER" 또는 "ROLE_LEADER")
```

**Response:**
- 성공 (200 OK): "회원가입 성공"
- 실패 (409 Conflict): "이미 존재하는 이메일입니다."
- 실패 (400 Bad Request): "인증 코드가 유효하지 않습니다."

**설명:**
사용자 회원가입을 처리합니다. 이메일 중복 확인 및 인증 코드 검증 후 사용자 정보를 저장합니다. 비밀번호는 BCrypt로 암호화됩니다.

---

### 2. 이메일 중복 확인

**URL:** `/account/postCheckEmailId`

**Method:** `POST`

**Request Body:**
```
emailId: String (이메일 주소)
```

**Response:**
- 성공 (200 OK): "사용 가능한 이메일입니다."
- 실패 (409 Conflict): "이미 존재하는 이메일입니다."

**설명:**
회원가입 시 이메일 주소의 중복 여부를 확인합니다.

---

## 이메일 인증 API

### 1. 인증 코드 발송

**URL:** `/email/verification`

**Method:** `POST`

**Request Body:**
```json
{
  "email": "user@example.com",
  "purpose": "signup"
}
```

**Response:**
- 성공 (200 OK): "Verification code sent to user@example.com"
- 실패 (500 Internal Server Error): 이메일 발송 실패 관련 메시지

**설명:**
사용자에게 이메일 인증 코드를 발송합니다. 인증 코드는 Redis에 저장되며 설정된 시간(기본 15분)동안 유효합니다.

---

### 2. 인증 코드 검증

**URL:** `/email/verify`

**Method:** `POST`

**Request Body:**
```json
{
  "email": "user@example.com",
  "authCode": "123456",
  "purpose": "signup"
}
```

**Response:**
- 성공 (200 OK): "인증 성공"
- 실패 (400 Bad Request): "인증 실패"

**설명:**
사용자가 입력한 인증 코드의 유효성을 검증합니다. Redis에 저장된 코드와 일치하는지 확인합니다.

---

## 워크스페이스 관리 API

### 1. 워크스페이스 생성

**URL:** `/api/workspaces`

**Method:** `POST`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "name": "새 워크스페이스"
}
```

**Response:**
- 성공 (201 Created):
```json
{
  "success": true,
  "message": "워크스페이스가 성공적으로 생성되었습니다.",
  "data": {
    "id": "uuid",
    "name": "새 워크스페이스",
    "createdAt": "2025-01-09T10:00:00",
    "updatedAt": "2025-01-09T10:00:00"
  }
}
```
- 실패 (400 Bad Request): 잘못된 요청 (유효성 검증 실패)
- 실패 (401 Unauthorized): 인증 실패

**설명:**
새로운 워크스페이스를 생성합니다. 워크스페이스 생성과 동시에 기본 페이지가 자동으로 생성되며, 생성한 사용자가 워크스페이스의 소유자(OWNER)가 됩니다.

---

### 2. 워크스페이스 조회

**URL:** `/api/workspaces/{workspaceId}`

**Method:** `GET`

**Headers:**
- `Authorization: Bearer {token}`

**Path Variable:**
- `workspaceId`: 워크스페이스 ID (UUID)

**Response:**
- 성공 (200 OK):
```json
{
  "success": true,
  "message": "워크스페이스 조회가 완료되었습니다.",
  "data": {
    "id": "uuid",
    "name": "워크스페이스명",
    "createdAt": "2025-01-09T10:00:00",
    "updatedAt": "2025-01-09T10:00:00"
  }
}
```
- 실패 (404 Not Found): 워크스페이스를 찾을 수 없음
- 실패 (403 Forbidden): 접근 권한 없음

**설명:**
특정 워크스페이스의 정보를 조회합니다. 해당 워크스페이스의 멤버만 조회할 수 있습니다.

---

### 3. 워크스페이스 삭제

**URL:** `/api/workspaces/{workspaceId}`

**Method:** `DELETE`

**Headers:**
- `Authorization: Bearer {token}`

**Path Variable:**
- `workspaceId`: 워크스페이스 ID (UUID)

**Response:**
- 성공 (200 OK):
```json
{
  "success": true,
  "message": "워크스페이스가 성공적으로 삭제되었습니다.",
  "data": null
}
```
- 실패 (404 Not Found): 워크스페이스를 찾을 수 없음
- 실패 (403 Forbidden): 삭제 권한 없음 (OWNER/ADMIN만 가능)

**설명:**
워크스페이스를 삭제합니다. 연관된 모든 페이지와 블록, 사용자 관계도 함께 삭제됩니다. OWNER 또는 ADMIN 권한이 필요합니다.

---

### 4. 사용자 초대 (TODO)

**URL:** `/api/workspaces/{workspaceId}/invite`

**Method:** `POST`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Path Variable:**
- `workspaceId`: 워크스페이스 ID (UUID)

**Request Body:**
```json
{
  "email": "user@example.com",
  "role": "MEMBER",
  "message": "워크스페이스에 초대합니다.",
  "expirationDays": 7
}
```

**Response:**
- 성공 (200 OK):
```json
{
  "success": true,
  "message": "초대 이메일이 발송되었습니다.",
  "data": "초대가 처리 중입니다."
}
```

**설명:**
워크스페이스에 새로운 사용자를 초대합니다. 초대 이메일이 발송되며, 초대받은 사용자는 링크를 통해 워크스페이스에 참여할 수 있습니다.

---

### 5. 사용자 역할 변경 (TODO)

**URL:** `/api/workspaces/{workspaceId}/users/role`

**Method:** `PUT`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "userId": 123,
  "role": "ADMIN",
  "reason": "관리자 권한 부여"
}
```

**설명:**
워크스페이스 내 사용자의 역할을 변경합니다. OWNER 또는 ADMIN 권한이 필요합니다.

---

### 6. 사용자 제거 (TODO)

**URL:** `/api/workspaces/{workspaceId}/users`

**Method:** `DELETE`

**Request Body:**
```json
{
  "userId": 123,
  "reason": "프로젝트 종료"
}
```

**설명:**
워크스페이스에서 사용자를 제거합니다. OWNER 또는 ADMIN 권한이 필요합니다.

---

### 7. 워크스페이스 멤버 조회 (TODO)

**URL:** `/api/workspaces/{workspaceId}/members`

**Method:** `GET`

**설명:**
워크스페이스의 모든 멤버 목록을 조회합니다.

---

## 에러 응답 형식

모든 API는 다음과 같은 표준화된 에러 응답 형식을 사용합니다:

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

### 주요 에러 상태 코드
- **400 Bad Request**: 잘못된 요청 (유효성 검증 실패)
- **401 Unauthorized**: 인증 실패
- **403 Forbidden**: 권한 없음
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 중복된 자원
- **500 Internal Server Error**: 서버 내부 오류