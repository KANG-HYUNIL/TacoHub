 
 

import { API_ENDPOINTS } from '../constants/api';
import type { PostSignupRequest, PostSignupResponse, ErrorResponse, RefreshTokenResponse, GetAccountInfoResponse } from '../types/api';
import { AccountDto, LogInDto } from '../types/dto';


/** 
 * 회원가입 API 호출 함수
 * @param emailId - 이메일(ID)
 * @param password - 비밀번호
 * @param name - 이름
 * @param authCode - 인증번호
 * @returns 성공 시 true, 실패 시 false
 */
export async function signup(
  emailId: string,
  password: string,
  name: string,
  authCode: string
): Promise<boolean> {
  /**
   * 1. 회원가입 요청 DTO 생성
   * 2. 회원가입 API 경로에 authCode, purpose("SignUp") 경로 변수 포함
   * 3. axios로 회원가입 API 호출 (반환 타입 명시)
   * 4. 정상 응답 시 성공 알림
   * 5. 비정상 응답 시 ApiResponse 기반 에러 메시지 알림
   */
  // 1. 회원가입 요청 DTO 생성 (role은 비워둠)
  const signupDto: PostSignupRequest = {
    emailId,
    password,
    name,
    role: '',
  };
  // 2. 회원가입 API 경로 생성
  const apiUrl: string = `/account/postSignup/${authCode}/SignUp`;
  try {
    // 3. axios로 회원가입 API 호출 (반환 타입 명시)
    const response = await axios.post<PostSignupResponse | ErrorResponse>(apiUrl, signupDto, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    // 4. 정상 응답 시 성공 알림 (타입 가드 활용)
    if (isPostSignupResponse(response.data)) {
      if (response.data.success) {
        alert('회원가입이 완료되었습니다!');
        return true;
      } else {
        let errorMsg: string = response.data.message ?? '회원가입 실패';
        console.log('[회원가입 실패] 서버 응답:', response.data);
        alert(errorMsg);
        return false;
      }
    } else {
      // 5. 비정상 응답 시 ApiResponse 기반 에러 메시지 알림
      let errorMsg: string = response.data.message ?? '회원가입 실패';
      console.log('[회원가입 비정상 응답] 응답 구조:', response.data);
      alert(errorMsg);
      return false;
    }
  } catch (error: any) {
    // 5. 비정상 응답 시 ApiResponse 기반 에러 메시지 알림
    let errorMsg: string = '회원가입 요청 실패';
    if (error.response && error.response.data && typeof error.response.data.message === 'string') {
      errorMsg = error.response.data.message;
      console.log('[회원가입 요청 실패] 서버 에러 응답:', error.response.data);
    } else if (error.message) {
      errorMsg = error.message;
      console.log('[회원가입 요청 실패] 에러 메시지:', error.message);
    } else {
      console.log('[회원가입 요청 실패] 알 수 없는 에러:', error);
    }
    alert(errorMsg);
    return false;
  }
}

/**
 * PostSignupResponse 타입 가드 함수
 * @param res - API 응답 데이터
 * @returns PostSignupResponse 타입 여부
 */
export function isPostSignupResponse(res: PostSignupResponse | ErrorResponse): res is PostSignupResponse {
  // success가 true이고, data가 string 또는 null이면 회원가입 성공 응답
  return typeof res.success === 'boolean' && (typeof res.data === 'string' || res.data === null);
}








/**
 * 로그인 API 호출 함수
 * @param id - 이메일/아이디
 * @param pw - 비밀번호
 * @returns 성공 시 true, 실패 시 false
 */
export async function login(id: string, pw: string): Promise<boolean> {
  /**
   * 1. 입력값을 DTO로 변환
   * 2. axios로 로그인 API 호출 (withCredentials로 쿠키 저장)
   * 3. 응답 status 200이면 accessToken을 localStorage에 저장, 성공 알림
   * 4. 실패 시 서버 메시지 추출하여 에러 알림
   */
  // 1. 입력값을 DTO로 변환
  const loginDto: LogInDto = {
    emailId: id,
    password: pw,
  };
  try {
    // 2. axios로 로그인 API 호출 (withCredentials로 쿠키 저장)
    const response = await axios.post<{ message?: string }>(API_ENDPOINTS.LOGIN, loginDto, {
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    });
    // 3. 응답 status 200이면 accessToken을 localStorage에 저장, 성공 알림
    if (response.status === 200) {
      // 응답 헤더에서 access token 추출
      const accessToken: string = response.headers['access'];
      if (accessToken) {
        // access token을 localStorage에 저장
        saveAccessToken(accessToken);
      }
      alert('로그인 성공!');
      return true;
    } else {
      // 4. 실패 시 서버 메시지 추출하여 에러 알림
      let errorMsg = '로그인 실패';
      if (response.data && typeof response.data.message === 'string') {
        errorMsg = response.data.message;
      }
      console.log('[로그인 실패] 서버 응답:', response.data);
      alert(errorMsg);
      return false;
    }
  } catch (error: any) {
    // 4. 실패 시 서버 메시지 추출하여 에러 알림
    let errorMsg = '로그인 요청 실패';
    if (error.response && error.response.data && typeof error.response.data.message === 'string') {
      errorMsg = error.response.data.message;
      console.log('[로그인 요청 실패] 서버 에러 응답:', error.response.data);
    } else if (error.message) {
      errorMsg = error.message;
      console.log('[로그인 요청 실패] 에러 메시지:', error.message);
    } else {
      console.log('[로그인 요청 실패] 알 수 없는 에러:', error);
    }
    alert(errorMsg);
    return false;
  }
}
/**
 * LocalStorage에 key-value를 저장하는 함수
 * @param key 저장할 key
 * @param value 저장할 value
 */
export function setLocalStorage(key: string, value: string): void {
  // 1. key와 value를 받아 localStorage에 저장
  localStorage.setItem(key, value);
}

/**
 * LocalStorage에서 key로 value를 가져오는 함수
 * @param key 가져올 key
 * @returns value (없으면 null)
 */
export function getLocalStorage(key: string): string | null {
  // 1. key를 받아 localStorage에서 value 반환
  return localStorage.getItem(key);
}

/**
 * LocalStorage에서 access token을 가져오는 함수
 * @returns access token (없으면 null)
 */
export function getAccessToken(): string | null {
  // 1. 내부적으로 getLocalStorage를 이용해 access token 반환
  return getLocalStorage('access');
}

/**
 * access token을 받아 localStorage에 저장하는 함수
 * @param accessToken 저장할 access token
 */
export function saveAccessToken(accessToken: string): void {
  // 1. access token을 localStorage에 'access' key로 저장
  setLocalStorage('access', accessToken);
}


function isRefreshTokenResponse(res : RefreshTokenResponse | ErrorResponse) : res is RefreshTokenResponse
{
    return res.success;
}


function isGetAccountInfoRequest (res : GetAccountInfoResponse | ErrorResponse) : res is GetAccountInfoResponse
{
  return res.success;
}


/**
 * access token을 헤더에 자동으로 포함하여 axios 요청을 보내는 공통 유틸 함수
 *
 * @template T - 기대하는 응답 데이터 타입 (예: ApiResponse, ErrorResponse 등)
 * @template U - 요청 바디 데이터 타입 (기본값: any)
 * @param apiUrl - 요청할 API 엔드포인트(경로)
 * @param bodyData - 요청 바디 데이터(POST/PUT 등에서 사용, GET/DELETE는 생략 가능)
 * @param method - HTTP 메서드 ('GET' | 'POST' | 'PUT' | 'DELETE' 등)
 * @param typeGuard - 응답 데이터가 기대하는 타입(T)인지 판별하는 타입 가드 함수 (선택)
 * @param extraHeaders - 추가로 포함할 헤더 객체 (선택)
 * @returns Promise<{ success: boolean; data: T | null; }> 
 *          - success: 요청 및 타입가드 통과 여부
 *          - data: 응답 데이터(성공 시 ApiResponse, 실패 시 ErrorResponse 등)
 *
 * 비즈니스 로직에서는 이 함수의 반환값의 success와 data를 활용하여 분기 처리하면 됨.
 */
export async function axiosWithAccessToken<T, U = any>
(
  apiUrl: string,
  bodyData?: U,
  method: 'get' | 'post' | 'put' | 'delete' = 'post',
  typeGuard?: (data: any) => data is T,
  extraHeaders?: Record<string, string>,
  maxRetry : number = 2
): Promise<{ success: boolean; apiResponse: T | null | ErrorResponse }>
{
  // 1. access token 획득
  const accessToken : string | null = getAccessToken();
  
  // 재시도 횟수
  let retryCnt :number = 0;

  while (retryCnt <= maxRetry) {
    try {
      const response = await axios({
        url: apiUrl,
        method: method,
        data: bodyData,
        headers: {
          'Content-Type': 'application/json',
          'access': accessToken,
          ...extraHeaders,
        },
        withCredentials: true,
      });

      if (typeGuard && typeGuard(response.data)) {
        return { success: true, apiResponse: response.data };
      } else if (response.status === 406) {
        const isRefreshed = await checkAccessTokenAndTryRefresh(response);
        if (isRefreshed) {
          retryCnt++;
          console.log('[axiosWithAccessToken] 406 발생, 토큰 재발급 후 재시도:', { retryCnt, apiUrl });
          continue;
        }
        console.log('[axiosWithAccessToken] 406 발생, 토큰 재발급 실패 또는 불가:', response.data);
        return { success: false, apiResponse: response.data as ErrorResponse };
      } else {
        console.log('[axiosWithAccessToken] 타입가드 실패 또는 비정상 응답:', response.data);
        return { success: false, apiResponse: response.data as ErrorResponse };
      }
    } catch (error: any) {
      if (error.response && error.response.data) {
        console.log('[axiosWithAccessToken] 네트워크/서버 에러 응답:', error.response.data);
      } else if (error.message) {
        console.log('[axiosWithAccessToken] 네트워크/서버 에러 메시지:', error.message);
      } else {
        console.log('[axiosWithAccessToken] 알 수 없는 네트워크/서버 에러:', error);
      }
      return { success: false, apiResponse: error.response?.data ?? null };
    }
  }
  console.log('[axiosWithAccessToken] 최대 재시도 초과:', { apiUrl, maxRetry });
  return { success: false, apiResponse: null };

}

/**
 * 406 응답이면 tryRefreshAccessToken을 호출하는 공통 메서드
 * @param error axios error 객체
 * @returns 토큰 재발급 성공 여부 (boolean)
 */
export async function checkAccessTokenAndTryRefresh(error: any): Promise<boolean> {
  if (error.response && error.response.status === 406) {
    return await tryRefreshAccessToken();
  }
  return false;
}

/**
 * /api/auth/refresh API를 통해 refresh token으로 access token 재발급 시도
 * @returns 성공 시 true, 실패 시 false
 */
export async function tryRefreshAccessToken(): Promise<boolean> {
  // 1. /api/auth/refresh 엔드포인트로 axios POST 요청
  // 2. 성공 시 access token을 localStorage에 저장
  // 3. 실패 시 false 반환
  try {
    const response = await axios.post<RefreshTokenResponse | ErrorResponse>(API_ENDPOINTS.REFRESH_TOKEN, {}, { withCredentials: true });
    
    const resData = response.data;

    if (response.status >= 200 &&
      response.status < 300 &&
      resData?.data) {

      // response를 refresh token response type로 지정하기
      
      //응답 header에 있는 access token을 저장해야 함, data가 아님
        const accessToken = response.headers['access'];
        if (accessToken) {
          saveAccessToken(accessToken);
        }
      
      return true;
    }
    return false;
  } catch (error) {
    return false;
  }
}

/**
 * access token을 이용해 /account/getAccountInfo API로 계정 정보(AccountDto) 요청
 * @returns AccountDto 또는 null
 */
export async function fetchAccountInfo(): Promise<AccountDto | null> {
  // 1. access token을 localStorage에서 가져옴
  const accessToken = getAccessToken();
  if (!accessToken) return null;
  // 2. access token을 헤더에 담아 axios POST 요청
  try {
    const response = await axios.post<GetAccountInfoResponse | ErrorResponse>(API_ENDPOINTS.ACCOUNT_GET_INFO, {}, {
      headers: {
        'Content-Type': 'application/json',
        'access': accessToken,
      },
    });

    const resData = response.data;

    // 정상 응답, 계정 정보 획득 성공
    if (response.status >= 200 && response.status < 300 && isGetAccountInfoRequest(resData)) {
      return resData.data ?? null;
    }
    // 비정상 응답, 계정 정보 획득 실패
    else {
      // 500번대 서버 에러: 별도 알림
      if (response.status >= 500 && response.status < 600) {
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        return null;
      }
      // 400번대 인증 관련 에러: return null (토큰 만료 등)
      if (response.status >= 400 && response.status < 500) {
        return null;
      }
      // 기타 예외 상황: return null
      return null;
    }
  } catch (error) {
    return null;
  }
}

 