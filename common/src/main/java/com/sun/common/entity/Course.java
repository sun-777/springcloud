package com.sun.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * @description: Course entity, mapping course table in Database
 * @author: Sun Xiaodong
 */

@javax.persistence.Entity
@Table(name = "course")
@Access(AccessType.FIELD)
@Cacheable
/** https://blog.csdn.net/liu_yulong/article/details/84594771 */
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
public class Course implements Entity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true, name="id", nullable = false, updatable = false, length = 24)
    // 设置自定义主键
    @GeneratedValue(strategy = GenerationType.AUTO, generator="customIdGenerator")
    // 指定主键生成器
    @GenericGenerator(name="customIdGenerator", strategy = "com.sun.common.id.CustomIdGenerator")
    private String id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;


    @ManyToOne(targetEntity = Teacher.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "tid", nullable=false, foreignKey = @ForeignKey(name="fk_teacher_id", value=ConstraintMode.NO_CONSTRAINT))
    @JsonIgnoreProperties(value = {"courses"})
    private Teacher teacher;

    public Course() {}
    
    public Course(final Course course) {
        this.withId(course.getId())
            .withName(course.getName())
            .withTeacher(course.getTeacher());
    }
    

    public Course(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    /**
     * 设置为私有成员方法，不允许应用程序自己设置id
     * @param id
     */
    private void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    private Course withId(String id) {
        setId(id);
        return this;
    }

    public Course withName(String name) {
        setName(name);
        return this;
    }
    
    public Course withTeacher(Teacher teacher) {
        setTeacher(teacher);
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
        Course course = (Course) o;
        return Objects.equals(id, course.id)
                       && Objects.equals(name, course.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
          sb.append(getClass().getSimpleName())
          .append(" {id='").append(id).append('\'')
          .append(", name='").append(name).append('\'')
          .append(", teachers='").append(teacher).append('\'')
          .append('}');
        return sb.toString();
    }
}
