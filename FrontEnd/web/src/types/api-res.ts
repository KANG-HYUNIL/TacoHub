// NotionCopyDTO Response 타입 예시
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  timestamp?: string;
  errorCode?: string;
}
// ...추가 응답 타입은 실제 DTO 구조에 맞게 계속 추가
