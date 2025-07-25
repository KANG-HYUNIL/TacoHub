/**
 * @fileoverview Aws 설정 파일
 * 
 * 이 파일은 TacoHub WebSocket 서버의 AWS 관련 설정을 구성합니다:
 * 1. CloudWatch Logs 설정
 * 2. S3 버킷 설정
 * 3. IAM 역할 및 정책 설정
 * 
 * 실시간 WebSocket 서비스의 안정성과 확장성을 위한
 * 체계적인 AWS 인프라 구성을 제공합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

// AWS SSM Package
import { SSMClient, GetParameterCommand } from '@aws-sdk/client-ssm';

// AWS S3 Package
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';



// -- SSM Parameter Store 설정 --

// AWS SSM 클라이언트 초기화
const ssm = new SSMClient({ region: process.env.AWS_REGION });

// 현재 프로파일 설정
const profile = process.env.NODE_ENV || 'test';
const profilePrefix = `/tacohub/websocket/${profile}`;

/**
 * AWS SSM 파라미터 스토어에서 비밀 값을 가져오는 함수
 * @param paramName {string} - SSM 파라미터 이름
 * @returns {Promise<string | undefined>} 비밀 값
 */
export async function getSecret(paramName: string): Promise<string | undefined> {
    const fullParamName = `${profilePrefix}/${paramName}`;
    const command = new GetParameterCommand({ Name: fullParamName, WithDecryption: true });
    const response = await ssm.send(command);
    return response.Parameter?.Value;
}



// -- S3 설정 --

// AWS S3 클라이언트 초기화
const s3 = new S3Client({
    region: process.env.AWS_REGION,
    credentials: {
        accessKeyId: process.env.AWS_ACCESS_KEY_ID || '',
        secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY || ''
    }
})


/**
 * S3 버킷에 객체를 업로드하는 함수
 * @param bucketName {string} - S3 버킷 이름
 * @param key {string} - S3 객체 키
 * @param body {Buffer | string} - 업로드할 객체의 내용
 */
export async function UploadToS3(bucketName : string, key : string, body: Buffer | string) {
    const command = new PutObjectCommand({
        Bucket: bucketName,
        Key: key,
        Body: body
    });
    await s3.send(command);
}


/**
 * Application Log S3 Bucket에 로그를 업로드하는 함수
 * @param key {string} - S3 객체 키
 * @param body {Buffer | string} - 업로드할 로그 내용
 */
export async function UploadApplicationLogToS3(key: string, body: Buffer | string) {

    const applicationBucket : string = `/s3/bucket/application-logs`;
    const bucketName : string | undefined = await getSecret(applicationBucket);

    // S3 버킷에 로그 업로드
    if (bucketName) {
        await UploadToS3(bucketName, key, body);
    }
}

/**
 * Audit Log S3 Bucket에 로그를 업로드하는 함수
 * @param key {string} - S3 객체 키 (예: 'audit/2025-07-25/connect-12345-20250725T120000Z.json')
 * @param body {Buffer | string} - 업로드할 로그 내용
 * key는 S3 내에서 파일을 구분하는 경로/이름으로, 보통 'audit/{날짜}/{이벤트명}-{userId}-{timestamp}.json' 형태로 생성
 */
export async function UploadAuditLogToS3(key: string, body: Buffer | string) {

    const auditBucket : string = `/s3/bucket/audit-logs`;
    const bucketName : string | undefined = await getSecret(auditBucket);

    // S3 버킷에 로그 업로드
    if (bucketName) {
        await UploadToS3(bucketName, key, body);
    }
}


