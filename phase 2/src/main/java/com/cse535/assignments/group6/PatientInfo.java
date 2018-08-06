package com.cse535.assignments.group6;

public final class PatientInfo {
    private String name, id, sex;
    private int age;

    public PatientInfo(String name, String id, String sex, int age) {
        this.name = name.toLowerCase();
        this.id = id.toLowerCase();
        this.sex = sex.toLowerCase();
        this.age = age;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getSex() {
        return this.sex;
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public String toString() {
        return String.format("%s_%s_%d_%s", this.name, this.id, this.age, this.sex);
    }
}
