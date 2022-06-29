package com.sun.common.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description: 自定义主键生成器
 * @author: Sun Xiaodong
 */
public class CustomIdGenerator extends IdentityGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor s, Object obj) {
        final IdGenerator.Id id = getIdGenerator();
        if (null != id) {
            return id.generator();
        } else {
            throw new IllegalArgumentException("custom id generator cannot be null");
        }
    }



    private static IdGenerator.Id getIdGenerator() {
        return IdGeneratorHolder.getIdGenerator();
    }

    public static void setIdGenerator(IdGenerator.Id idGenerator) {
        IdGeneratorHolder.setIdGenerator(idGenerator);
    }

    /**
     * Id生成器Holder，存放yaml配置中指定的生成器
     * @author Sun Xiaodong
     */
    private static final class IdGeneratorHolder {
        private static final List<IdGenerator.Id> HOLDER = new ArrayList<>(0);
        private static final List<IdGenerator.Id> ID_GENERATOR = Collections.unmodifiableList(HOLDER);

        static IdGenerator.Id getIdGenerator() {
            return 0 == ID_GENERATOR.size() ? null : ID_GENERATOR.get(0);
        }

        static void setIdGenerator(IdGenerator.Id idGenerator) {
            HOLDER.add(idGenerator);
        }
    }
}
