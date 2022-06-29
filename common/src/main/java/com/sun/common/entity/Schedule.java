package com.sun.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.common.enumeration.Lesson;
import com.sun.common.enumeration.Weekday;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

/**
 * @description: Schedule entity, mapping schedule table in Database
 * @author: Sun Xiaodong
 */
@javax.persistence.Entity
@Table(name = "schedule")
@Access(AccessType.FIELD)
@Cacheable
/** https://blog.csdn.net/liu_yulong/article/details/84594771 */
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
public class Schedule implements Entity {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false, length = 24)
    // 设置自定义主键
    @GeneratedValue(strategy = GenerationType.AUTO, generator="customIdGenerator")
    // 指定主键生成器
    @GenericGenerator(name="customIdGenerator", strategy = "com.sun.common.id.CustomIdGenerator")
    private String id;

    // 周几
    @Column(name = "weekday")
    // @Type 是Hibernate JPA必须项，指明自定义类型Weekday持久化到数据库表中转换规则
    @Type(type = "com.sun.common.typehandler.WeekdayType")
    private Weekday weekday;

    // 课序
    @Column(name = "lesson")
    // @Type 是Hibernate JPA必须项，指明自定义类型Lesson持久化到数据库表中转换规则
    @Type(type = "com.sun.common.typehandler.LessonType")
    private Lesson lesson;

    // 课程
    @OneToOne(targetEntity = Course.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "cid", nullable = false, foreignKey = @ForeignKey(name="fk_course_id", value=ConstraintMode.NO_CONSTRAINT))
    private Course course;

    public Schedule() {}

    public Schedule(final Schedule schedule) {
        this.withId(schedule.getId())
            .withWeekday(schedule.getWeekday())
            .withLesson(schedule.getLesson())
            .withCourse(schedule.getCourse());
    }

    public Schedule(Course course, Weekday weekday, Lesson lesson) {
        this.course = course;
        this.weekday = weekday;
        this.lesson = lesson;
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

    public Weekday getWeekday() {
        return weekday;
    }

    public void setWeekday(Weekday weekday) {
        this.weekday = weekday;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    private Schedule withId(String id) {
        setId(id);
        return this;
    }

    public Schedule withCourse(Course course) {
        setCourse(course);
        return this;
    }

    public Schedule withWeekday(Weekday weekday) {
        setWeekday(weekday);
        return this;
    }

    public Schedule withLesson(Lesson lesson) {
        setLesson(lesson);
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
        Schedule schedule = (Schedule) o;
        return Objects.equals(id, schedule.id)
                       && weekday == schedule.weekday
                       && lesson == schedule.lesson
                       && Objects.equals(course, schedule.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, weekday, lesson, course);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName())
          .append(" {").append(" id = '").append(id).append('\'')
          .append(",  weekday = ").append(weekday.name())
          .append(",  lesson = ").append(lesson.name())
          .append(",  course = ").append(course)
          .append('}');
        return sb.toString();
    }
}
