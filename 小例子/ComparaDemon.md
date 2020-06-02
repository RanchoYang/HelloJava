## ComparaDemon

```java
package com.demon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComparaDemon {
    
    public static void main(String[] args) {
        Person person1 = new Person(1, "TOM");
        Person person2 = new Person(2, "Ana");
        Person person3 = new Person(3, "JAMES");
        Person person4 = new Person(4, "ALEN");
        Person person5 = new Person(5, "ERIC");
        List<Person> persons = new ArrayList<Person>();
        persons.add(person1);
        persons.add(person2);
        persons.add(person3);
        persons.add(person4);
        persons.add(person5);

        // 1、通过实现comparable 名字排序
        // Collections.sort(persons);

        // 2、通过实现Comparator 名字倒序排序
        // Collections.sort(persons, new PersonNameSort());

        // 3、通过实现Comparator id排序
        Collections.sort(persons, new PersonIdSort());
        System.out.println(persons);
    }
}

class Person implements Comparable<Person> {
    
    private Integer id;
    private String name;

    public Person(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("id=");
        sb.append(this.id);
        sb.append(",name=");
        sb.append(this.name);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(Person o) {
        return name.compareTo(o.getName());
    }
}

class PersonNameSort implements Comparator<Person>{
    
    @Override
    public int compare(Person o1, Person o2) {
        return o2.getName().compareTo(o1.getName());
    }
}

class PersonIdSort implements Comparator<Person>{
    
    @Override
    public int compare(Person o1, Person o2) {
        return o1.getId().compareTo(o2.getId());
    }
}
```
