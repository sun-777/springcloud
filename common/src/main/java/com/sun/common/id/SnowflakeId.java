package com.sun.common.id;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @description: SnowflakeId id generator： a way to generate id
 * @author: Sun Xiaodong
 */
public class SnowflakeId implements Generator {
    private static final long serialVersionUID = 1L;

    // 开始时间截 2022-1-1 00:00:00:000
    public static final long EPOCH;

    // 序列占有的位数
    private static final int SEQUENCE_BITS = 12;

    // 机器id占有的位数
    private static final int WORKER_ID_BITS = 10;

    // 生成序列的掩码: 4095
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS); // (1 << SEQUENCE_BITS) - 1

    // 机器ID左移偏移量（12）
    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;

    // 时间截向左移偏移量(10+12)
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;

    // 支持的最大数据标识id，结果是31
    public static final long WORKER_ID_MAX_VALUE = -1L ^ (-1L << WORKER_ID_BITS);  // (1 << WORKER_ID_BITS) - 1

    // 设置默认的震颤周期值为：[0, 1]， 可设置的最大值为：SEQUENCE_MASK
    private static final int DEFAULT_VIBRATION_VALUE = 1;

    // 最大可容忍的时间差（10ms）
    private static final int MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 10;

    private static final TimeService TIME_SERVICE = new TimeService();

    /**
     * 工作机器ID(0~1023)
     */
    private static volatile long WORKER_ID = 0;

    /**
     * 序列增量
     */
    private int sequenceOffset = -1;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;


    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.FEBRUARY, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
    }


    public static final SnowflakeId INSTANCE = new SnowflakeId();


    private SnowflakeId() {}

    private static long getWorkerId() {
        return WORKER_ID;
    }

    public static void setWorkId(long workId) {
        WORKER_ID = workId;
    }


    public synchronized long nextId() {
        long currentTimestamp = TIME_SERVICE.getCurrentMillis();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，则抛出异常
        if (waitTolerateTimeDifferenceIfNeed(currentTimestamp)) {
            currentTimestamp = TIME_SERVICE.getCurrentMillis();
        }

        if (lastTimestamp == currentTimestamp) {  // 如果是同一时间生成的，则进行毫秒内序列
            // 毫秒内序列溢出
            if (0L == (sequence = (sequence + 1) & SEQUENCE_MASK)) {
                //阻塞到下一个毫秒,获得新的时间戳
                currentTimestamp = waitUntilNextTime(lastTimestamp);
            }
        } else {
            vibrateSequenceOffset();
            sequence = sequenceOffset;
        }
        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (getWorkerId() << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
    }


    private boolean waitTolerateTimeDifferenceIfNeed(final long currentTimestamp) {
        if (lastTimestamp <= currentTimestamp) {
            return false;
        }
        long timeDiffMillis = lastTimestamp - currentTimestamp;
        if ( timeDiffMillis > MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS) {
            throw new IllegalStateException(String.format("Clock moved backwards, last time is %d milliseconds, current time is %d milliseconds", lastTimestamp, currentTimestamp));
        }

        try {
            TimeUnit.MILLISECONDS.sleep(timeDiffMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private long waitUntilNextTime(final long lastTimestamp) {
        long currentTimestamp = TIME_SERVICE.getCurrentMillis();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = TIME_SERVICE.getCurrentMillis();
        }
        return currentTimestamp;
    }


    /**
     * 标准的雪花算法，在生成id时，如果本次生成id的时间与上一次生成id的时间不是同一毫秒，即跨了毫秒，则序列部分会从0开始计算。
     * 如果不是高并发环境下，每次生成id都可能跨毫秒，这样每次生成的id都是偶数，
     * 如果根据id进行奇偶分片，则数据全部落到偶数表里面了，这种结果肯定不是我们期望的。
     * 我们期望的结果是，数据能均匀的分布到奇偶表中，那么跨毫秒生成的id的序列就不能一直从0开始。
     */
    private void vibrateSequenceOffset() {
        sequenceOffset = sequenceOffset >= DEFAULT_VIBRATION_VALUE ? 0 : sequenceOffset + 1;
    }

    /**
     * Time service.
     */
    static final class TimeService {

        public long getCurrentMillis() {
            return System.currentTimeMillis();
        }
    }
}
