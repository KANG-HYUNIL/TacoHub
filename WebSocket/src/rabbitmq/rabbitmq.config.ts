/**
 * @fileoverview RabbitMQ 설정 파일
 * 
 * 이 파일은 TacoHub WebSocket 서버의 RabbitMQ 설정을 구성합니다:
 * 1. RabbitMQ 서버 주소 및 포트 설정
 * 2. 연결 타임아웃 및 재시도 설정
 * 3. 메시지 큐 및 교환기 설정
 * 4. 메시지 직렬화 및 역직렬화 설정
 * 
 * 실시간 WebSocket 서비스의 안정적인 메시징을 위한
 * 체계적인 RabbitMQ 인프라를 제공합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

// RabbitMQ 연결 설정
import * as amqp from 'amqplib';
import { applicationLogger } from '../utils/logger';
import { getSecret } from '../config/aws';
import { handleError } from '../utils/error-handler';
import { RabbitMQError } from '../types/error/RabbitMQError';


// RabbitMQ Prefix 
const RABBITMQ_PREFIX = 'tacohub.ws.'; // RabbitMQ 큐 이름 접두사

// API 서버용 RabbitMQ 상수
export const API_SERVER_QUEUE = 'tacohub.api.server.shared';
export const COLLABORATION_EXCHANGE = 'tacohub.collaboration.exchange';
export const API_SERVER_ROUTING_KEY = 'api.#';

// RabbitMQ 서버 주소 및 포트 설정

/**
 * RabbitMQ 에 필요한 값들 설정하는 드
 */
async function getRabbitMQConfig() : Promise<{ uri: string; }>
{
    let uri : string ; // RabbitMQ 서버 URI

    const methodName = 'getRabbitMQConfig';

    try {
        applicationLogger.info(`[${methodName}] RabbitMQ 설정값을 불러옵니다. 환경: ${process.env.NODE_ENV}`);
        if (process.env.NODE_ENV === 'local') {
        applicationLogger.info(`[${methodName}] Local 환경: compose.yaml 기본값 사용`);
        return { uri: 'amqp://guest:guest@localhost:5672' };
        }

        // test, prod 환경: Parameter Store에서 값 가져오기
        const host = await getSecret(`/rabbitmq/host`);
        const port = await getSecret(`/rabbitmq/port`);
        const username = await getSecret(`/rabbitmq/username`);
        const password = await getSecret(`/rabbitmq/password`);
        const vhost = await getSecret(`/rabbitmq/virtual-host`);

        applicationLogger.info(`[${methodName}] Parameter Store에서 값 불러옴: host=${host}, port=${port}, username=${username}, vhost=${vhost}`);

        // amqplib URI 포맷: amqp://username:password@host:port/vhost
        const uri = `amqp://${username}:${password}@${host}:${port}/${vhost}`;
        applicationLogger.info(`[${methodName}] RabbitMQ URI 생성: ${uri}`);
        return { uri };

    } catch (error: unknown) {
        handleError(new RabbitMQError(`[${methodName}] RabbitMQ 설정값을 불러오는 중 오류 발생: ${error instanceof Error ? error.message : String(error)}`), methodName);
    }
 
}

/**
 * RabbitMQ 연결을 생성하는 함수
 */
export async function createRabbitMQConnection(): Promise<amqp.Channel> {
    const methodName = 'createRabbitMQConnection';

    try {
        applicationLogger.info(`[${methodName}] RabbitMQ 연결 시도`);

        // 1. RabbitMQ 설정값 가져오기
        const config = await getRabbitMQConfig();
        const url = config.uri;

        // 2. RabbitMQ 연결 생성
        const connection = await amqp.connect(url);

        // 3. Channel 생성
        const channel = await connection.createChannel();

        applicationLogger.info(`[${methodName}] RabbitMQ 연결 성공: ${url}`);

        return channel;
    } catch (error) {
        handleError(new RabbitMQError(`[${methodName}] RabbitMQ 연결 실패: ${error instanceof Error ? error.message : String(error)}`), methodName);
    }
}

/**
 * 서버 전용 큐를 연결(없으면 생성)하는 함수
 * @param channel RabbitMQ channel
 * @param serverId 서버 고유 ID (컨테이너 이름 등)
 * @returns channel, queueName
 */
export async function connectServerQueue(channel: amqp.Channel, serverId: string): Promise<{ channel: amqp.Channel, queueName: string }> {
    const methodName = 'connectServerQueue';

    try {
        // 서버별 큐 이름: prefix + serverId
        const queueName = `${RABBITMQ_PREFIX}${serverId}`;
        await channel.assertQueue(queueName, { durable: true });

        applicationLogger.info(`[${methodName}] 서버 전용 큐 연결/생성: ${queueName}`);

        return { channel, queueName };
    } catch (error) {
        handleError(new RabbitMQError(`[${methodName}] 서버 전용 큐 연결/생성 실패: ${error instanceof Error ? error.message : String(error)}`), methodName);
    }
}


/**
 * Collaboration Exchange와 Server Queue를 Binding 하는 메서드
 * @param channel RabbitMQ channel
 * @param exchangeName 교환기 이름
 * @param queueName 큐 이름
 * @param routingKey 라우팅 키 (기본값: '')
 * @returns void
 */
export async function bindExchangeToQueue(
    channel : amqp.Channel,
    exchangeName : string,
    queueName : string,
    routingKey : string,
    type : 'topic' | 'fanout' = 'topic'
) : Promise<void>
{
    const methodName = 'bindExchangeToQueue';

    try {

        // Exchange가 존재하지 않으면 생성
        await channel.assertExchange(exchangeName, type, { durable: true });

        // Queue가 존재하지 않으면 생성
        await channel.assertQueue(queueName, { durable: true });

        // Exchange와 Queue를 Binding
        await channel.bindQueue(queueName, exchangeName, routingKey);
        applicationLogger.info(`[${methodName}] Exchange와 Queue 바인딩 성공: ${exchangeName} -> ${queueName} (Routing Key: ${routingKey})`);
    
    } 
    catch (error) 
    {
        handleError(new RabbitMQError(`[${methodName}] ${exchangeName} Exchange와 ${queueName} Queue 바인딩 실패: ${error instanceof Error ? error.message : String(error)}`), methodName);
    }
}

/**
 * Collaboration Exchange와 Topic Queue를 Binding 하는 메서드
 * @param channel RabbitMQ channel
 * @returns void
 */
export async function bindCollaborationExchangeToTopicApiQueue(channel: amqp.Channel): Promise<void> 
{
    await bindExchangeToQueue(
        channel,
        COLLABORATION_EXCHANGE,
        API_SERVER_QUEUE,
        API_SERVER_ROUTING_KEY,
        'topic'
    )

}

/**
 * Collaboration Exchange와 Server 자신 Queue를 fanout 방식으로 Binding 하는 메서드
 * @param channel RabbitMQ channel
 * @param queueName 서버 자신 큐 이름
 * @returns void
 */
export async function bindCollaborationExchangeToServerQueue(
    channel: amqp.Channel, 
    queueName: string)
    : Promise<void>
{
    await bindExchangeToQueue(
        channel,
        COLLABORATION_EXCHANGE,
        queueName,
        '', // fanout 방식은 라우팅 키가 필요 없음
        'fanout'
    )
}



/**
 * RabbitMQ 시작 함수
 * RabbitMQ 연결을 생성하고, 서버 전용 큐를 연결하며,
 * Collaboration Exchange와 Topic Queue를 Binding 합니다.
 * 또한 Api Server Queue와도 Binding 하며,
 * Broadcast 메시징을 위한 설정을 포함합니다.
 * @returns {Promise<void>}
 */
export async function startRabbitMQ() : Promise<void> 
{
    // 1. RabbitMQ 연결 생성
    const channel : amqp.Channel = await createRabbitMQConnection();

    // 2. Server 전용 큐 연결
    const serverId = process.env.SERVER_ID || 'default-server';
    const {queueName} = await connectServerQueue(channel, serverId);

    // 3. Collaboration Exchange와 Topic Api Queue Binding
    await bindCollaborationExchangeToTopicApiQueue(channel);

    // 4. Collaboration Exchange와 Server Queue Fanout Binding(Broadcast)
    await bindCollaborationExchangeToServerQueue(channel, queueName);

}


