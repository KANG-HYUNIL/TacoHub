/**
 * @fileoverview 로거 유틸리티 재export
 * 
 * 이 파일은 중앙집중식 로깅 시스템에 대한 접근점을 제공합니다.
 * 다른 모듈에서 `import { logger } from '../utils/logger'` 형태로 
 * 일관된 방식으로 로거에 접근할 수 있도록 합니다.
 * 
 * 향후 로깅 시스템 변경 시 이 파일만 수정하면 되므로
 * 유지보수성과 확장성을 높입니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

export {applicationLogger, auditLogger, AuditLogDecorator} from '../config/logger';
