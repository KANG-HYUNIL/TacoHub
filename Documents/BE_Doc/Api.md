# TacoHub API 문서

## 목차
1. [사용자 계정 관리 API](#사용자-계정-관리-api)
2. [이메일 인증 API](#이메일-인증-api)

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