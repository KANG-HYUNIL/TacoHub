# EmailConfig

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>어노테이션</th><td>@Configuration</td></tr>
  <tr><th>클래스 설명</th><td>JavaMailSender 등 이메일 발송 환경을 설정하는 Spring Configuration 클래스.<br>인증코드, 알림 등 다양한 이메일 발송 기능을 지원한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>host</td><td>String</td><td>SMTP 서버 호스트 주소. application.yml에서 주입받음.</td></tr>
  <tr><td>port</td><td>int</td><td>SMTP 서버 포트 번호. application.yml에서 주입받음.</td></tr>
  <tr><td>username</td><td>String</td><td>이메일 발송 계정 아이디. application.yml에서 주입받음.</td></tr>
  <tr><td>password</td><td>String</td><td>이메일 발송 계정 비밀번호. 환경변수 또는 외부 설정파일에서 주입받음.</td></tr>
  <tr><td>protocol</td><td>String</td><td>SMTP 프로토콜 타입(smtp, smtps 등). application.yml에서 주입받음.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>EmailConfig()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>javaMailSender()</td>
    <td>SMTP 설정값을 기반으로 JavaMailSender Bean을 생성.<br>host, port, username, password, protocol을 적용하여 이메일 발송 환경을 구성한다.</td>
    <td>없음 (모든 값은 클래스 멤버에서 주입)</td>
    <td>JavaMailSender<br>(org.springframework.mail.javamail.JavaMailSender)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. application.yml의 이메일 설정값을 읽어 필드에 주입한다.
2. `javaMailSender()`가 호출되어 JavaMailSender Bean을 생성한다.
3. 인증코드, 알림 등에서 JavaMailSender를 DI 받아 이메일을 발송한다.

## 활용 예시 (Usage)
- 인증코드 발송, 알림 메일, 각종 시스템 이메일 발송 등.

## 예외 및 주의사항 (Exceptions & Notes)
- SMTP 서버 연결 실패 시 예외 발생 가능. host/port/username/password/protocol 설정을 반드시 확인할 것.
- 비밀번호(password)는 환경변수 또는 외부 설정파일로 관리하여 보안에 유의할 것.
