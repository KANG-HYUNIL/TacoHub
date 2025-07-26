export class PresenceError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'PresenceError';
    }
}