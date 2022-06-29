package com.sun.server.context.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @description: 自定义的主键配置
 * @author: Sun Xiaodong
 */

@Configuration
@ConfigurationProperties(prefix = "spring.id-generator")
public class IdGeneratorProperties {

    private SnowflakeId snowflakeId;
    private ObjectId objectId;


    public SnowflakeId getSnowflakeId() {
        return snowflakeId;
    }

    public void setSnowflakeId(SnowflakeId snowflakeId) {
        this.snowflakeId = snowflakeId;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }




    public class ObjectId {
        private boolean enable = false;

        public boolean getEnable() {
            return this.enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }



    public static final class SnowflakeId {
        private boolean enable = false;
        private Set<String> workId;

        public boolean getEnable() {
            return this.enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public Set<String> getWorkId() {
            return this.workId;
        }

        public void setWorkId(Set<String> workId) {
            this.workId = workId;
        }

    }


}
