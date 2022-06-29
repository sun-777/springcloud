package com.sun.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.common.enumeration.Gender;
import com.sun.common.util.Constants;
import com.sun.common.util.LocalDateUtil;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @description: Teacher entity, mapping teacher table in Database
 * @author: Sun Xiaodong
 */
@javax.persistence.Entity
@Table(name = "teacher")
@Access(AccessType.FIELD)
@Cacheable
/** https://blog.csdn.net/liu_yulong/article/details/84594771 */
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
public class Teacher implements Entity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false, length = 24)
    // 设置自定义主键
    @GeneratedValue(strategy = GenerationType.AUTO, generator="customIdGenerator")
    // 指定主键生成器
    @GenericGenerator(name="customIdGenerator", strategy = "com.sun.common.id.CustomIdGenerator")
    private String id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Transient
    private Integer age;

    @Column(name = "gender")
    // @Type 是Hibernate JPA必须项，指明自定义类型Gender持久化到数据库表中转换规则
    @Type(type = "com.sun.common.typehandler.GenderType")
    private Gender gender;

    @Column(name = "birth", nullable = true)
    private LocalDate birth;

    // 双向必设mappedBy，mappedBy相当于（inverse=true）
    @OneToMany(targetEntity = Course.class, mappedBy = "teacher", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"teacher"})
    private Set<Course> courses;


    public Teacher() {}

    public Teacher(final Teacher teacher) {
        this.withId(teacher.getId())
            .withName(teacher.getName())
            .withGender(teacher.getGender())
            .withBirth(teacher.getBirth())
            .withCourses(teacher.getCourses());
    }

    public Teacher(String name, Gender gender, LocalDate birth) {
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        setAge(this.birth);
    }


    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        setAge(birth);
        this.birth = birth;
    }

    private void setAge(LocalDate birth) {
        if (null != birth) {
            // 根据生日自动生成年龄
            final Period period = birth.until(LocalDate.now(ZoneId.of(Constants.DEFAULT_TIME_ZONE)));
            this.age = (0 == period.getMonths() && 0 == period.getDays()) ? period.getYears() : period.getYears() + 1;
        }
    }


    public Integer getAge(Integer age) {
        return this.age;
    }


    public Set<Course> getCourses() {
        LazyInitCourses();
        return this.courses;
    }


    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    private void LazyInitCourses() {
        if (null == this.courses) {
            this.courses = new HashSet<>(0);
        }
    }


    private Teacher withId(String id) {
        setId(id);
        return this;
    }

    public Teacher withName(String name) {
        setName(name);
        return this;
    }

    public Teacher withGender(Gender gender) {
        setGender(gender);
        return this;
    }

    public Teacher withBirth(LocalDate birth) {
        setBirth(birth);
        return this;
    }

    public Teacher withCourses(Set<Course> courses) {
        setCourses(courses);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Teacher teacher = (Teacher) o;
        return Objects.equals(id, teacher.id)
                       && Objects.equals(name, teacher.name)
                       && Objects.equals(age, teacher.age)
                       && gender == teacher.gender
                       && Objects.equals(birth, teacher.birth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, gender, birth);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName())
          .append(" {id='").append(id).append('\'')
          .append(", name='").append(name).append('\'')
          .append(", age='").append(age).append('\'')
          .append(", gender='").append(gender.name()).append('\'')
          .append(", birth='").append(LocalDateUtil.toString(birth)).append('\'')
          .append(", courses='").append(courses).append('\'')
          .append('}');
        return sb.toString();
    }
}
