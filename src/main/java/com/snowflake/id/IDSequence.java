package com.snowflake.id;
/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Mr.Yangxiufeng
 * Date: 2018-04-16
 * Time: 15:48
 * http://git.oschina.net/yu120/sequence
 */
public class IDSequence extends AbsSequence{

    public IDSequence() {
        super();
    }

    public IDSequence(long workerId, long dataCenterId) {
        super(workerId, dataCenterId);
    }
    /**
     * 获取下一个ID
     */
    public synchronized Long nextId() {
        long timestamp = timestamp();
        return ((timestamp - twepoch) << timestampLeftShift)    // 时间戳部分
                | (dataCenterId << dataCenterIdShift)           // 数据中心部分
                | (workerId << workerIdShift)                   // 机器标识部分
                | sequence;                                     // 序列号部分
    }
}
