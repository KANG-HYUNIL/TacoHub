# AwsS3Config

**패키지:** com.example.TacoHub.Config

## 1. 개요
`AwsS3Config`는 AWS S3 연동을 위한 인증 정보와 리전(region) 설정을 관리하고, AmazonS3 클라이언트 Bean을 생성하는 Spring @Configuration 클래스입니다. S3 기반 감사 로그 저장, 파일 업로드 등에서 핵심적으로 사용됩니다.

## 2. 의존성 및 환경
- **Spring Framework**: @Configuration, @Bean, @Value
- **AWS SDK**: com.amazonaws.services.s3.AmazonS3 등
- **설정 파일**: application.yml 또는 application.properties에서 AWS 관련 설정값을 주입받음

## 3. 클래스 멤버 및 의미
- `accessKey` (`String`): AWS IAM Access Key. S3 접근 인증에 사용
- `secretKey` (`String`): AWS IAM Secret Key. S3 접근 인증에 사용
- `region` (`String`): S3가 위치한 AWS 리전(예: ap-northeast-2)

각 멤버는 `@Value` 어노테이션을 통해 외부 설정값을 주입받으며, 보안상 외부 노출 금지 필요

## 4. 메서드 상세 설명
### 4.1 `AmazonS3 s3Client()`
- **역할**: AWS S3와 통신할 수 있는 AmazonS3 클라이언트 객체를 Bean으로 등록
- **반환값**: `AmazonS3` 인스턴스 (싱글턴)
- **동작**:
  1. `accessKey`, `secretKey`로 `BasicAWSCredentials` 객체 생성
  2. `AWSStaticCredentialsProvider`로 인증 정보 래핑
  3. 지정된 `region`으로 AmazonS3ClientBuilder를 통해 클라이언트 생성
  4. 생성된 AmazonS3 객체를 Spring Bean으로 등록
- **예외 처리**: AWS SDK 내부에서 인증 정보가 잘못되었거나 네트워크 오류 발생 시 런타임 예외 발생 가능
- **사용 예시**: S3 파일 업로드/다운로드, 감사 로그 저장 등에서 DI로 주입받아 사용

#### 인자
없음 (모든 값은 클래스 멤버에서 주입)

#### 반환값
- `AmazonS3`: AWS S3와 연동 가능한 클라이언트 객체

#### 예외
- AWS 인증 정보가 잘못되었거나 네트워크 장애 시 AWS SDK에서 예외 발생

## 5. 동작 흐름
1. Spring 컨테이너가 AwsS3Config를 초기화할 때, application.yml의 AWS 설정값을 읽어 멤버 변수에 주입
2. `s3Client()` 메서드가 호출되어 AmazonS3 Bean이 생성됨
3. 이후 S3 연동이 필요한 서비스/컴포넌트에서 DI로 AmazonS3 객체를 주입받아 사용

## 6. 활용 예시
- S3 기반 감사 로그 저장 서비스에서 파일 업로드/다운로드
- 대용량 데이터 백업, 정적 파일 관리 등

## 7. 보안 및 운영상 주의사항
- Access Key/Secret Key는 절대 코드에 하드코딩하지 않고, 환경변수 또는 외부 설정파일로 관리
- IAM 권한 최소화 원칙 적용
- 리전(region) 설정이 실제 S3 버킷과 일치해야 정상 동작
