
/**
 * @fileoverview 실시간 사용자 presence(접속/퇴장/상태) 관리 서비스
 *
 * 이 파일은 WebSocket 서버에서 사용자의 워크스페이스/페이지 입장, 퇴장, 온라인/오프라인 상태를 메모리 기반으로 관리합니다.
 * - 사용자의 입장/퇴장 시 Map/Set 자료구조에 등록/제거
 * - 현재 접속 중인 사용자 목록 조회
 * - 페이지/워크스페이스별 사용자 상태 동기화 및 브로드캐스트 지원
 *
 * @author TacoHub Team
 * @version 1.0.0
 */





/**
 * 페이지별 접속 사용자 목록을 관리하는 Map
 * key: roomName(workspaceId-pageId), value: Set<userId>
 */
export const pagePresenceMap = new Map<string, Set<string>>();


/**
 * 사용자별 소켓 ID 목록을 관리하는 Map
 * key: userId, value: Set<socketId>
 */
export const userSocketsMap = new Map<string, Set<string>>();

/**
 * 사용자를 특정 페이지(room)에 등록
 * @param roomName - 워크스페이스/페이지 조합(room) 이름
 * @param userId - 사용자 ID
 * @param socketId - 소켓 ID
 */
export function addUserToPage(
    roomName: string, 
    userId: string, 
    socketId: string) {

    // Page에 유저 등록
    if (!pagePresenceMap.has(roomName)) {
        pagePresenceMap.set(roomName, new Set());
    }

    pagePresenceMap.get(roomName)!.add(userId);



    // UserId 별 Socket 등록
    if (!userSocketsMap.has(userId)) {
        userSocketsMap.set(userId, new Set());
    }

    userSocketsMap.get(userId)!.add(socketId);

}

/**
 * 사용자를 특정 페이지(room)에서 제거
 * @param roomName - 워크스페이스/페이지 조합(room) 이름
 * @param userId - 사용자 ID
 * @param socketId
 */
export function removeUserFromPage(
    roomName: string, 
    userId: string,
    socketId : string) {
    
    // Page에서 유저 제거
    if (pagePresenceMap.has(roomName)) 
        {
        const users = pagePresenceMap.get(roomName)!;
        users.delete(userId);
        
        // 페이지에 더 이상 사용자가 없으면 Map에서 제거
        if (users.size === 0) 
        {
            pagePresenceMap.delete(roomName);
        }
    }


    // UserId에서 Socket 제거
    if (userSocketsMap.has(userId))
    {
        const sockets = userSocketsMap.get(userId)!;
        sockets.delete(socketId);

        // 사용자에 더 이상 소켓이 없으면 Map에서 제거
        if (sockets.size === 0)
        {
            userSocketsMap.delete(userId);
        }
    }

}

/**
 * 특정 페이지(room)에 접속 중인 사용자 목록 조회
 * @param roomName - 워크스페이스/페이지 조합(room) 이름
 * @returns Set<string> - 해당 room에 접속 중인 userId 목록
 */
export function getUsersInPage(roomName: string): Set<string> {
    return pagePresenceMap.get(roomName) || new Set();
}




/**
 * 사용자 별 Socket ID 목록 조회
 * @param userId - 사용자 ID
 * @returns Set<string> - 해당 사용자에 연결된 Socket ID 목록
 */
export function getSocketsByUser(userId: string): Set<string> {
    return userSocketsMap.get(userId) || new Set();
}



