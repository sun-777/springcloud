package com.sun.common.id;

/**
 * @description: 生成ID
 * @author: Sun Xiaodong
 */
public interface IdGenerator {

    String generator();

    public enum Id implements IdGenerator {
        SNOWFLAKEID {
            @Override
            public String generator() {
                return String.valueOf(SnowflakeId.INSTANCE.nextId());
            }
        },
        OBJECTID {
            @Override
            public String generator() {
                return ObjectId.get().toHexString();
            }
        }
    }

}
