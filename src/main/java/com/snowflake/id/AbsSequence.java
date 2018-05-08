package com.snowflake.id;

import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
* Created with IntelliJ IDEA.
 * Description:
 * User: Mr.Yangxiufeng
 * Date: 2018-04-16
 * Time: 16:58
 */
public abstract class AbsSequence{
    /* 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动） */
    protected static final long twepoch = 1288834974657L;
    protected static final long workerIdBits = 5L;/* 机器标识位数 */
    protected static final long dataCenterIdBits = 5L;
    protected static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    protected static final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
    protected static final long sequenceBits = 12L;/* 毫秒内自增位 */
    protected static final long workerIdShift = sequenceBits;
    protected static final long dataCenterIdShift = sequenceBits + workerIdBits;
    /* 时间戳左移动位 */
    protected static final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    protected static final long sequenceMask = -1L ^ (-1L << sequenceBits);

    protected long workerId;

    /* 数据标识id部分 */
    protected long dataCenterId;
    protected long sequence = 0L;/* 0，并发控制 */
    protected long lastTimestamp = -1L;/* 上次生产id时间戳 */

    public AbsSequence() {
        this.dataCenterId = getDataCenterId(maxDataCenterId);
        this.workerId = getMaxWorkerId(dataCenterId, maxWorkerId);
    }

    /**
     * @param workerId     工作机器ID
     * @param dataCenterId 序列号
     */
    public AbsSequence(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * <p>
     * 获取 maxWorkerId
     * </p>
     */
    private static long getMaxWorkerId(long dataCenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(dataCenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotEmpty(name)) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * <p>
     * 数据标识id部分
     * </p>
     */
    private static long getDataCenterId(long maxDataCenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (maxDataCenterId + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * 获取下一个ID
     * @return object
     */
    public abstract Object nextId();

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return SystemClock.now();
    }
    protected long timestamp(){
        long timestamp = timeGen(); //获取当前毫秒数
        //如果服务器时间有问题(时钟后退) 报错。
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        //如果上次生成时间和当前时间相同,在同一毫秒内
        if (lastTimestamp == timestamp) {
            //sequence自增，因为sequence只有12bit，所以和sequenceMask相与一下，去掉高位
            sequence = (sequence + 1) & sequenceMask;
            //判断是否溢出,也就是每毫秒内超过4095，当为4096时，与sequenceMask相与，sequence就等于0
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp); //自旋等待到下一毫秒
            }
        } else {
            sequence = 0L; //如果和上次生成时间不同,重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加
        }
        lastTimestamp = timestamp;
        return timestamp;
    }
}
