export class BusinessError extends Error {
  public code?: string;
  constructor(message: string, code?: string) {
    super(message);
    this.name = 'BusinessError';
    this.code = code;
  }
}
