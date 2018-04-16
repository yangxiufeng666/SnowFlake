package com.snowflake.id;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Mr.Yangxiufeng
 * Date: 2018-04-16
 * Time: 16:11
 */
public class TimeSequence extends AbsSequence{

    public TimeSequence() {
        super();
    }

    public TimeSequence(long workerId, long dataCenterId) {
        super(workerId, dataCenterId);
    }

    @Override
    public synchronized String nextId() {
        long timestamp = timestamp();
        long suffix = (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
        String datePrefix = DateFormatUtils.format(timestamp, "yyyyMMddHHMMssSSS");

        return datePrefix + suffix;
    }
}
