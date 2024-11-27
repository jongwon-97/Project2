package com.kosmo.nexus.common.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // 비밀번호 해싱
    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    // 비밀번호 검증
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
