import { ServerError } from './ServerError';

export class RabbitMQError extends ServerError {
  constructor(message: string, code?: string) {
    super(message, code);
    this.name = 'RabbitMQError';
  }
}
