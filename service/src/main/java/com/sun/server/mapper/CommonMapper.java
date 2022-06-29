package com.sun.server.mapper;

import com.sun.common.entity.Course;
import com.sun.common.entity.Entity;
import com.sun.common.entity.Schedule;
import com.sun.common.entity.Teacher;
import com.sun.common.enumeration.Gender;
import com.sun.common.enumeration.Lesson;
import com.sun.common.enumeration.Weekday;
import com.sun.common.vo.ScheduleofTeacherVo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sun.common.util.ReflectionUtil.getFieldName;

/**
 * @description: 无批处理的实体映射类
 *               Criteria使用： https://docs.oracle.com/cd/E19798-01/821-1841/gjitv/index.html
 *                             persistence-2_0-final-spec.pdf
 * @author: Sun Xiaodong
 */
public class CommonMapper<T extends Entity> extends AbstractEntityMapper<T> {
    private final EntityManager entityManager;

    public CommonMapper(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }


    public List<T> query(Class<T> clazz, final Map<String, Object> conditions) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<T> criteria = builder.createQuery(clazz);
            final Root<T> from = criteria.from(clazz);
            // 查询实体类的所有字段
            criteria.select(from);

            List<Predicate> predicates = new ArrayList<>(conditions.size());
            conditions.forEach((k, v) -> {
                Path<String> path = from.get(k);
                predicates.add(builder.equal(builder.lower(path), v));
            });

            criteria.where(predicates.toArray(new Predicate[0]));
            return em.createQuery(criteria).getResultList();
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    /**
     * SQL:
     *  select s.weekday, s.lesson, c.name, t.name
     *  from schedule as s
     *  left join course as c on c.id = s.cid
     *  left join teacher as t on t.id = c.tid
     *  where t.id = '<teacherId>';
     *
     * @param teacherId
     * @return
     */
    public List<ScheduleofTeacherVo> queryScheduleOfSpecifiedTeacher(final String teacherId) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<Schedule> root = criteria.from(Schedule.class);

            final String sid = getFieldName(Schedule::getId);
            final String sweekday = getFieldName(Schedule::getWeekday);
            final String slesson = getFieldName(Schedule::getLesson);
            final String scourse = getFieldName(Schedule::getCourse);
            final String cid = getFieldName(Course::getId);
            final String cname = getFieldName(Course::getName);
            final String cteacher = getFieldName(Course::getTeacher);
            final String tid = getFieldName(Teacher::getId);
            final String tname = getFieldName(Teacher::getName);
            final String tgender = getFieldName(Teacher::getGender);
            final String tbirth = getFieldName(Teacher::getBirth);
            Join<Schedule, Course> courseJoin = root.join(scourse, JoinType.LEFT);
            Join<Course, Teacher> teacherJoin = courseJoin.join(cteacher, JoinType.LEFT);

            // 别名数组
            final String[] aliases = new String[] {"s", "c", "t"};  // s:schedule; c:course; t:teacher;
            //criteria.select(builder.tuple(root.alias(aliases[0]), courseJoin.alias(aliases[1]), teacherJoin.alias(aliases[2])));
            criteria.multiselect(
                    root.<String>get(sid).alias(aliases[0].concat(sid)),
                    root.<String>get(sweekday).alias(aliases[0].concat(sweekday)),
                    root.<String>get(slesson).alias(aliases[0].concat(slesson)),
                    courseJoin.<String>get(cid).alias(aliases[1].concat(cid)),
                    courseJoin.<String>get(cname).alias(aliases[1].concat(cname)),
                    teacherJoin.<String>get(tid).alias(aliases[2].concat(tid)),
                    teacherJoin.<String>get(tname).alias(aliases[2].concat(tname)),
                    teacherJoin.<String>get(tgender).alias(aliases[2].concat(tgender)),
                    teacherJoin.<String>get(tbirth).alias(aliases[2].concat(tbirth))
            );
            criteria.where(builder.equal(builder.lower(teacherJoin.get(tid)), teacherId));

            List<Tuple> tupleList = em.createQuery(criteria).getResultList();
            List<ScheduleofTeacherVo> resultList = new ArrayList<>(tupleList.size());
            for (Tuple tuple : tupleList) {
                ScheduleofTeacherVo vo = new ScheduleofTeacherVo();
                /*Schedule s = tuple.get(aliases[0], Schedule.class);
                Course c = tuple.get(aliases[1], Course.class);
                Teacher t = tuple.get(aliases[2], Teacher.class);
                vo.setSid(s.getId())
                          .setSlesson(s.getLesson())
                          .setSweekday(s.getWeekday())
                          .setCid(c.getId())
                          .setCname(c.getName())
                          .setTid(t.getId())
                          .setTname(t.getName())
                          .setTgender(t.getGender())
                          .setTbirth(t.getBirth());*/
                vo.setSid(tuple.get(aliases[0].concat(sid), String.class))
                          .setSlesson(tuple.get(aliases[0].concat(slesson), Lesson.class))
                          .setSweekday(tuple.get(aliases[0].concat(sweekday), Weekday.class))
                          .setCid(tuple.get(aliases[1].concat(cid), String.class))
                          .setCname(tuple.get(aliases[1].concat(cname), String.class))
                          .setTid(tuple.get(aliases[2].concat(tid), String.class))
                          .setTname(tuple.get(aliases[2].concat(tname), String.class))
                          .setTgender(tuple.get(aliases[2].concat(tgender), Gender.class))
                          .setTbirth(tuple.get(aliases[2].concat(tbirth), LocalDate.class));
                resultList.add(vo);
            }
            return resultList;
        } catch (IllegalArgumentException | PersistenceException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    /**
     * SQL:
     *      select * from teacher as t
     *      where exists
     *          (select 1 from schedule as s
     *              left join course as c on c.id = s.cid
     *           where t.id = c.tid);
     * @return
     */
    public List<Teacher> queryAllTeacherFromSchedule() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<Teacher> criteria = builder.createQuery(Teacher.class);
            final Root<Teacher> from = criteria.from(Teacher.class);
            criteria.select(from);
            // 子查询
            Subquery<Schedule> subquery = criteria.subquery(Schedule.class);
            final Root<Schedule> subFrom = subquery.from(Schedule.class);
            subquery.select(subFrom);

            final String tid = getFieldName(Teacher::getId);
            final String scourse = getFieldName(Schedule::getCourse);
            final String ctid = getFieldName(Course::getTeacher);
            Join<Schedule, Course> courseJoin = subFrom.join(scourse, JoinType.LEFT);
            subquery.where(builder.equal(builder.lower(from.get(tid)), builder.lower(courseJoin.get(ctid))));

            criteria.where(builder.exists(subquery));
            List<Teacher> teacherList = em.createQuery(criteria).getResultList();
            return teacherList;
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    /**
     * 涉及"with as" SQL语法，没办法直接使用CriteriaBuilder查询。
     * 但是可以使用CriteriaBuilder构建等价SQL：
     *      select count(*) as count, c.name as name from schedule as s left join course as c on c.id = s.cid group by name;
     * 再对查询结果分析、获取最大数量的课目。
     *
     * SQL:
     *      with t as (select count(*) as count, c.name as name
     * 	        from schedule as s
     *          left join course as c on c.id = s.cid
     *          group by name)
     *      select t.count as count, t.name as name
     *      from t
     *      group by name
     *      having count = (SELECT MAX(t.count) FROM t);
     *
     */
    public Map<String, Long> getMaxCountCourseFromSchedule() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            /** 原生SQL查询
            Query nativeQuery = em.createNativeQuery(
                    "with t as (select count(*) as count, c.name as name from schedule as s left join course as c on c.id = s.cid group by name)\n" +
                    "select t.count as count, t.name as name from t group by name having count = (SELECT MAX(t.count) FROM t);");
            final List<Object[]> resultList = nativeQuery.getResultList();
            final Map<String, Long> map = new HashMap<>(resultList.size());
            resultList.forEach(arr -> map.put((String) arr[1], Long.valueOf(((BigInteger) arr[0]).longValue())));
            return map;
             */
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<Schedule> root = criteria.from(Schedule.class);
            final String cname = getFieldName(Course::getName);
            final Join<Schedule, Course> courseJoin = root.join(getFieldName(Schedule::getCourse), JoinType.LEFT);

            final String[] aliases = new String[] {"count", "name"};
            criteria.multiselect(
                    builder.count(courseJoin.<String>get(cname)).as(Long.class).alias(aliases[0]),
                    courseJoin.<String>get(cname).alias(aliases[1]));
            criteria.groupBy(courseJoin.<String>get(cname));
            final List<Tuple> tupleList = em.createQuery(criteria).getResultList();

            // 降序排序
            final List<Tuple> desSortedTupleList = tupleList.stream().sorted((o1, o2) -> {
                    final Long l1 = o1.get(aliases[0], Long.class);
                    final Long l2 = o2.get(aliases[0], Long.class);
                    return l2.compareTo(l1);
            }).collect(Collectors.toList());

            // 分析查询结果，获取一周中的最多数量的科目
            final Map<String, Long> map = new HashMap<>();
            Long max = 0L;
            for (Tuple tuple : desSortedTupleList) {
                final Long count = tuple.get(aliases[0], Long.class);
                final String name = tuple.get(aliases[1], String.class);
                if (count > max) {
                    max = count;
                    map.put(name, max);
                } else if (count == max){
                    map.put(name, max);
                } else {
                    break;
                }
            }
            return map;
        } catch (IllegalArgumentException | PersistenceException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }

}
