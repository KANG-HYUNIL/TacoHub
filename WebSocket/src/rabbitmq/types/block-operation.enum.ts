// 기준: 2025-07-25 NotionCopy Message Spec v1.0
export enum BlockOperation {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  MOVE = 'move',
  DUPLICATE = 'duplicate',
  CONVERT_TYPE = 'convert_type',
  INDENT = 'indent',
  OUTDENT = 'outdent',
}
