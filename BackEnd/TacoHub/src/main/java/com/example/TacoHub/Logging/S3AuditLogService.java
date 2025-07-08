package com.example.TacoHub.Logging;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * S3 기반 감사 로그 아카이빙 서비스
 * - 비용 효율적인 장기 보관
 * - 압축된 배치 업로드
 * - 자동 라이프사이클 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "audit.log.storage.type", havingValue = "s3-archive")
public class S3AuditLogService implements AuditLogService {

    private final AmazonS3 s3Client;
    private final ObjectMapper objectMapper;
    
    // 메모리 버퍼 (배치 처리용)
    private final List<AuditLog> logBuffer = new CopyOnWriteArrayList<>();
    
    // 설정값 주입 (application.yml 전역 AWS 설정에서 가져옴)
    @Value("${cloud.aws.s3.bucket:tacohub-audit-logs}")
    private String bucketName;
    
    @Value("${cloud.aws.s3.batch-size:1000}")
    private int batchSize;
    
    @Value("${cloud.aws.s3.flush-interval:300000}")
    private long flushInterval;

    @Override
    public void save(AuditLog auditLog) {
        // 실시간 저장은 하지 않고 버퍼에 추가
        logBuffer.add(auditLog);
        
        // 버퍼가 가득 차면 즉시 업로드
        if (logBuffer.size() >= batchSize) {
            flushToS3();
        }
    }

    @Override
    @Async("auditLogExecutor")
    public void saveAsync(AuditLog auditLog) {
        save(auditLog);
    }

    /**
     * 5분마다 버퍼의 로그를 S3에 업로드
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void scheduledFlush() {
        if (!logBuffer.isEmpty()) {
            flushToS3();
        }
    }

    /**
     * 버퍼의 로그들을 압축하여 S3에 업로드
     */
    private void flushToS3() {
        if (logBuffer.isEmpty()) return;

        try {
            // 현재 버퍼 내용을 복사하고 클리어
            List<AuditLog> logsToUpload = List.copyOf(logBuffer);
            logBuffer.clear();

            // JSON 배열로 변환
            String jsonLogs = objectMapper.writeValueAsString(logsToUpload);
            
            // GZIP 압축
            byte[] compressedData = compressData(jsonLogs.getBytes());
            
            // S3 키 생성 (날짜/시간 기반)
            String s3Key = generateS3Key();
            
            // S3 업로드 (개선된 메타데이터 포함)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(compressedData.length);
            metadata.setContentType("application/gzip");
            metadata.setContentEncoding("gzip");
            metadata.addUserMetadata("log-count", String.valueOf(logsToUpload.size()));
            metadata.addUserMetadata("original-size", String.valueOf(jsonLogs.length()));
            
            PutObjectRequest putRequest = new PutObjectRequest(
                bucketName, s3Key, new ByteArrayInputStream(compressedData), metadata);
            
            s3Client.putObject(putRequest);
            
            log.info("S3 감사 로그 업로드 완료: key={}, count={}, size={}KB", 
                    s3Key, logsToUpload.size(), compressedData.length / 1024);
                    
        } catch (Exception e) {
            log.error("S3 감사 로그 업로드 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 데이터를 GZIP으로 압축
     */
    private byte[] compressData(byte[] data) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    /**
     * S3 키 생성 (파티셔닝을 위한 날짜/시간 기반)
     * 예: audit-logs/2024/01/15/audit-logs-20240115-143022.json.gz
     */
    private String generateS3Key() {
        LocalDate now = LocalDate.now();
        String timestamp = java.time.LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        
        return String.format("audit-logs/%04d/%02d/%02d/audit-logs-%s.json.gz",
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(), timestamp);
    }
}
