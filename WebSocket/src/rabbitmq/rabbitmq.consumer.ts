
/**
 * @fileoverview RabbitMQ 메시지 Consumer
 *
 * 이 파일은 WebSocket 서버가 RabbitMQ 큐로부터 메시지를 수신(consume)하여
 * 실시간 협업 이벤트를 처리하는 역할을 담당합니다.
 *
 * 주요 기능:
 * - 지정된 큐에서 메시지 수신 및 파싱
 * - 메시지 타입에 따라 비즈니스 로직 분기 처리
 * - 메시지 처리 성공/실패 로깅 및 예외 처리
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

import * as amqp from 'amqplib';
import { applicationLogger } from '../utils/logger';
import * as RabbitMQ from './rabbitmq.config';
import { handleError } from '../utils/error-handler';
import { RabbitMQError } from '../types/error/RabbitMQError';
import { BaseMessage } from './types/base-message.types';
import { MessageType } from './types/message-type.enum';

/**
 * RabbitMQ 큐에서 메시지를 consume(수신)하는 함수
 * @param {string} queueName - 수신할 큐 이름
 * @returns {Promise<void>} - 메시지 consume 시작 완료 Promise
 */
export async function startConsumeFromQueue(queueName: string): Promise<void> {
    const methodName = 'startConsumeFromQueue';
    try {
        // 1. RabbitMQ 채널 싱글턴 획득
        const channel: amqp.Channel = await RabbitMQ.getRabbitMQChannelSingleton();

        // 2. 큐가 존재하지 않으면 생성
        await channel.assertQueue(queueName, { durable: true });

        // 3. consume 메서드로 메시지 수신 시작
        await channel.consume(queueName, async (msg) => {

            if (!msg) return;

            try {
                // 4-1. 메시지 파싱(JSON)
                const content = msg.content.toString();

                // 4-2. 메시지 타입/내용에 따라 분기 처리
                let parsed: BaseMessage;
                try 
                {
                    parsed = JSON.parse(content);

                } catch (parseError) 

                {
                    applicationLogger.error(`[${methodName}] 메시지 파싱 실패: ${content}`);
                    channel.nack(msg, false, false); // 파싱 실패 시 reject
                    return;
                }

                // 4-2. 메시지 타입/내용에 따라 분기 처리
                // TODO: 아래 switch/case 또는 if/else로 메시지 타입별 분기, 내부는 비워둠
                switch (parsed?.messageType) {

                    case MessageType.BLOCK:
                        // TODO: 블록 업데이트 메시지 처리


                        break;
                    case MessageType.PAGE:
                        // TODO: 페이지 입장 메시지 처리


                        break;
                    // ...다른 메시지 타입 추가
                    default:

                        // TODO: 알 수 없는 타입 처리
                        break;
                }

                // 4-3. 처리 성공 시 ack
                channel.ack(msg);
            } catch (error) {
                // 4-4. 예외 발생 시 로깅 및 에러 핸들러 호출, nack
                handleError(error, methodName);

            }
        });

        applicationLogger.info(`[${methodName}] Started consuming from queue: ${queueName}`);
    } catch (error) {


        handleError(
            new RabbitMQError(
                `[${methodName}] 큐에서 메시지 수신 시작 실패: ${error instanceof Error ? error.message : String(error)}`
            ), methodName);
    }
}
