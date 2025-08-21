/**
 * @fileoverview RabbitMQ 메시지 Producer
 *
 * 이 파일은 WebSocket 서버에서 발생한 실시간 협업 이벤트(예: 블록 편집, 페이지 변경 등)를
 * RabbitMQ Exchange/Queue로 publish(전송)하는 역할을 담당합니다.
 *
 * 주요 기능:
 * - 메시지 생성 및 직렬화
 * - 지정된 Exchange/Queue로 메시지 publish(큐에 넣기 또는 브로드캐스트)
 * - 메시지 전송 성공/실패 로깅 및 예외 처리
 *
 * @author TacoHub Team
 * @version 1.0.0
 */



import * as amqp from 'amqplib';
import { applicationLogger } from '../utils/logger';
import * as RabbitMQ from './rabbitmq.config';
import { RabbitMQError } from '../types/error/RabbitMQError';
import { handleError } from '../utils/error-handler';
import { BlockMessage } from './types/block-message.types';
import e from 'express';


/**
 * RabbitMQ Message를 지정된 Exchange로 Routing Key 통해 Publish 하는 함수
 * @param {string} exchange - RabbitMQ Exchange 이름
 * @param {string} routingKey - 메시지를 전송할 Routing Key
 * @param {object} message - 전송할 메시지 객체
 * @param {string} type - Exchange 타입 (기본값: 'topic')
 * @returns {Promise<void>} - 메시지 전송 완료 Promise
 */
export async function publishToExchange(
    exchange: string,
    routingKey: string,
    message: object,
    type: 'fanout' | 'topic' = 'topic'
) : Promise<void>
{
    const methodName = 'publishToExchange';

    try{

        // 1. RabbitMQ 채널 싱글턴 획득
        const channel : amqp.Channel = await RabbitMQ.getRabbitMQChannelSingleton();

        // 2. Exchange 선언 (fanout 또는 topic)
        await channel.assertExchange(exchange, type, { durable: true });

        // 3. 메시지 직렬화 (JSON 문자열로 변환)
        const payload = JSON.stringify(message);

        // 4. 메시지 Publish
        channel.publish(exchange, routingKey, Buffer.from(payload), { persistent: true });

        applicationLogger.info(`[RabbitMQ] Published message to exchange "${exchange}" with routing key "${routingKey}" (${type})`);

    } catch ( error)
    {
        handleError(
            new RabbitMQError(
                `Failed to publish message to exchange ${exchange} with routing key ${routingKey}`,
                error instanceof Error ? error.message : String(error)
            ),
            methodName
        )
    }

}


/**
 * RabbitMQ 메세지를 지정된 Exchange로 fanout 통한 Broadcast Publish 하는 함수
 * @param {string} exchange - RabbitMQ Exchange 이름
 * @param {object} message - 전송할 메시지 객체
 * @param {string} type - Exchange 타입 (기본값: 'fanout')
 * @returns {Promise<void>} - 메시지 전송 완료 Promise
 */
export async function publishToExchangeBroadcast(
    exchange: string,
    routingKey: string,
    message: object,
    type: 'fanout' | 'topic' = 'fanout'
) : Promise<void>
{
    const methodName = 'publishToExchangeBroadcast';

    try {

       // 1. RabbitMQ 채널 싱글턴 획득
        const channel : amqp.Channel = await RabbitMQ.getRabbitMQChannelSingleton();

        // 2. Exchange 선언 (fanout 또는 topic)
        await channel.assertExchange(exchange, type, { durable: true });

        // 3. 메시지 직렬화 (JSON 문자열로 변환)
        const payload = JSON.stringify(message);

        // 4. 메시지 Publish
        channel.publish(exchange, routingKey, Buffer.from(payload), { persistent: true });

        applicationLogger.info(`[RabbitMQ] Published message to exchange "${exchange}" with routing key "${routingKey}" (${type})`);


    } catch (error)
    {
        handleError(
            new RabbitMQError(
                `Failed to broadcast message to exchange ${exchange} with routing key ${routingKey}`,
                error instanceof Error ? error.message : String(error)
            ),
            methodName
        );
    }


}



/**
 * Block Update Message를 RabbitMQ Collaboration Ecvhange로 fanout Broadcast 하는 함수
 * @param {BlockMessage} blockMessage 
 * @returns {Promise<void>} - 메시지 전송 완료 Promise
 */
export async function publishBlockUpdateMessageToCollaborationExchangeByBroadcast(
    blockMessage : BlockMessage
) : Promise<void>
{
    publishToExchangeBroadcast(
        RabbitMQ.COLLABORATION_EXCHANGE,
        '',    
        blockMessage,
        'fanout'
    )
}
