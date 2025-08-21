# TacoHub WebSocket Server Dockerfile (TypeScript)
FROM node:20.11.1-alpine

# 작업 디렉토리 생성
WORKDIR /app

# package.json, package-lock.json, tsconfig.json 등 복사
COPY package*.json ./
COPY tsconfig.json ./
COPY .env* ./

# 의존성 설치
RUN npm install --production

# 소스 코드 복사
COPY src ./src
COPY public ./public
COPY . ./

# 빌드 (ts -> js)
RUN npm run build

# 환경변수: 서버 ID를 컨테이너 이름에서 추출할 수 있도록 설정
ENV SERVER_ID=""

# 컨테이너 실행 시 서버 ID를 docker 컨테이너 이름에서 추출
CMD ["sh", "-c", "export SERVER_ID=$(cat /etc/hostname) && node dist/server.js"]
