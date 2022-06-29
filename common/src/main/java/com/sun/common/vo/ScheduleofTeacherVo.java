package com.sun.common.vo;

import com.sun.common.enumeration.Gender;
import com.sun.common.enumeration.Lesson;
import com.sun.common.enumeration.Weekday;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public class ScheduleofTeacherVo implements Serializable {
    private static final long serialVersionUID = 1519434287465314058L;
    private String sid;
    private Weekday sweekday;
    private Lesson slesson;
    private String cid;
    private String cname;
    private String tid;
    private String tname;
    private Gender tgender;
    private LocalDate tbirth;

    public ScheduleofTeacherVo() {}

    public String getSid() {
        return sid;
    }

    public ScheduleofTeacherVo setSid(String sid) {
        this.sid = sid;
        return this;
    }

    public Weekday getSweekday() {
        return sweekday;
    }

    public ScheduleofTeacherVo setSweekday(Weekday sweekday) {
        this.sweekday = sweekday;
        return this;
    }

    public Lesson getSlesson() {
        return slesson;
    }

    public ScheduleofTeacherVo setSlesson(Lesson slesson) {
        this.slesson = slesson;
        return this;
    }

    public String getCid() {
        return cid;
    }

    public ScheduleofTeacherVo setCid(String cid) {
        this.cid = cid;
        return this;
    }

    public String getCname() {
        return cname;
    }

    public ScheduleofTeacherVo setCname(String cname) {
        this.cname = cname;
        return this;
    }

    public String getTid() {
        return tid;
    }

    public ScheduleofTeacherVo setTid(String tid) {
        this.tid = tid;
        return this;
    }

    public String getTname() {
        return tname;
    }

    public ScheduleofTeacherVo setTname(String tname) {
        this.tname = tname;
        return this;
    }

    public Gender getTgender() {
        return tgender;
    }

    public ScheduleofTeacherVo setTgender(Gender tgender) {
        this.tgender = tgender;
        return this;
    }

    public LocalDate getTbirth() {
        return tbirth;
    }

    public ScheduleofTeacherVo setTbirth(LocalDate tbirth) {
        this.tbirth = tbirth;
        return this;
    }
}
