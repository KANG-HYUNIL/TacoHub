/**
 * @fileoverview static-server 설정 파일. 포트/정적 파일 경로 등 관리.
 */
export const SERVER_CONFIG = {
  port: process.env.PORT || 3000,
  staticPath: '../../web/build',
};
