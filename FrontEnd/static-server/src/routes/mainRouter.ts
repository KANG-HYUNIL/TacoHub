/**
 * @fileoverview / 경로에 대한 정적 파일 서빙 라우터
 * - / 요청 시 web/build/index.html 파일을 반환
 */
import { Router, Request, Response } from 'express';
import path from 'path';

const router = Router();

router.get('*', (req: Request, res: Response) => {
  res.sendFile(path.join(__dirname, '../../web/build/index.html'));
});

export default router;
