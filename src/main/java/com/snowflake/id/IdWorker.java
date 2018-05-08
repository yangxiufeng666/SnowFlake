package com.snowflake.id;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Mr.Yangxiufeng
 * Date: 2018-04-16
 * Time: 14:17
 */
public class IdWorker {
    /**
     * 主机和进程的机器码
     */
    private static final IDSequence ID_SEQUENCE = new IDSequence();

    private static final TimeSequence TIME_SEQUENCE = new TimeSequence();

    public static long getIdSequence() {
        return ID_SEQUENCE.nextId();
    }
    public static String getTimeSequence(){
        return TIME_SEQUENCE.nextId();
    }
    /**
     * <p>
     * 获取去掉"-" UUID
     * </p>
     * @return string
     */
    public static synchronized String get32UUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
