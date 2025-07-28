/**
 * @fileoverview static-server 진입점. Express 서버 실행 및 정적 파일/라우팅 관리.
 */
import express from 'express';
import path from 'path';
import { SERVER_CONFIG } from './config/serverConfig';
import router from './routes';

const app = express();

// 정적 파일 서빙
app.use(express.static(path.join(__dirname, SERVER_CONFIG.staticPath)));

// 모든 라우팅은 router에서 관리
app.use('/', router);

app.listen(SERVER_CONFIG.port, () => {
  console.log(`Static server running at http://localhost:${SERVER_CONFIG.port}`);
});
