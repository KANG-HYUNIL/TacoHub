export class PageEditError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'PageEditError';
    }
}