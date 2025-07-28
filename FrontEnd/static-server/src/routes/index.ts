/**
 * @fileoverview 모든 라우터 엔트리. 하위 라우터(mainRouter, aboutRouter, loginRouter, signupRouter, descRouter, workspaceRouter 등) 연결.
 */
import { Router } from 'express';
import mainRouter from './mainRouter';


const router = Router();

router.use('/', mainRouter);


export default router;
