package com.security.scanner;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Worker {
  private String name;
  private int age;
  private long id;

  public Worker(String name, int age, long id) {
    this.name = name;
    this.age = age;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public long getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Worker worker = (Worker) o;
    return id == worker.id && Objects.equals(name, worker.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, id);
  }

  @Override
  public String toString() {
    return "Worker{" +
        "name='" + name + '\'' +
        ", age=" + age +
        ", id=" + id +
        '}';
  }
}

