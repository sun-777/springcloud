package com.sun.common.typehandler;

import com.sun.common.enumeration.Gender;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Optional;

/**
 * @description: Gender是自定义的枚举类型：根据Gender的code属性，持久化到数据库
 * @author: Sun Xiaodong
 */
public class GenderType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.SMALLINT};
    }

    @Override
    public Class<?> returnedClass() {
        return Gender.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hash(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        //从JDBC的ResultSet读取属性值，即：从数据库取数据。
        final short code = rs.getShort(names[0]);
        final Optional<Gender> optional = Gender.values()[0].codeOf(code);
        return optional.orElse(null);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        // value为枚举对象，将枚举对象的code属性写入数据库中
        if (null != value) {
            st.setShort(index, ((Gender) value).code());
        } else {
            st.setNull(index, sqlTypes()[0]);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
