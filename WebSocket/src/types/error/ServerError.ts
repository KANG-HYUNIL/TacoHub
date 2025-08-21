export class ServerError extends Error {
  public code?: string;
  constructor(message: string, code?: string) {
    super(message);
    this.name = 'ServerError';
    this.code = code;
  }
}
