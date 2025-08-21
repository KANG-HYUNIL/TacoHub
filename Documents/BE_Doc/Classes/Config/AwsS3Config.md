# AwsS3Config

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>어노테이션</th><td>@Configuration</td></tr>
  <tr><th>클래스 설명</th><td>AWS S3 연동을 위한 인증 정보 및 리전(region) 설정을 관리하고, AmazonS3 클라이언트 Bean을 생성하는 Spring Configuration 클래스.<br>S3 기반 감사 로그 저장, 파일 업로드 등에서 핵심적으로 사용된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>accessKey</td><td>String</td><td>AWS IAM Access Key. S3 접근 인증에 사용. application.yml에서 주입받음.</td></tr>
  <tr><td>secretKey</td><td>String</td><td>AWS IAM Secret Key. S3 접근 인증에 사용. application.yml에서 주입받음.</td></tr>
  <tr><td>region</td><td>String</td><td>S3가 위치한 AWS 리전(예: ap-northeast-2). application.yml에서 주입받음.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>AwsS3Config()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>s3Client()</td>
    <td>AWS S3와 통신할 수 있는 AmazonS3 클라이언트 Bean을 생성.<br>accessKey, secretKey, region을 기반으로 인증 및 리전 설정을 적용한다.</td>
    <td>없음 (모든 값은 클래스 멤버에서 주입)</td>
    <td>AmazonS3<br>(com.amazonaws.services.s3.AmazonS3)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. application.yml의 AWS 설정값을 읽어 필드에 주입한다.
2. `s3Client()`가 호출되어 AmazonS3 Bean을 생성한다.
3. S3 연동이 필요한 서비스/컴포넌트에서 DI로 AmazonS3 객체를 주입받아 사용한다.

## 활용 예시 (Usage)
- S3 기반 감사 로그 저장, 대용량 파일 업로드/다운로드, 정적 파일 관리 등.

## 예외 및 주의사항 (Exceptions & Notes)
- Access Key/Secret Key는 절대 코드에 하드코딩하지 않고, 환경변수 또는 외부 설정파일로 관리해야 한다.
- IAM 권한 최소화 원칙 적용.
- 리전(region) 설정이 실제 S3 버킷과 일치해야 정상 동작한다.
- AWS 인증 정보가 잘못되었거나 네트워크 장애 시 AWS SDK에서 예외 발생.
