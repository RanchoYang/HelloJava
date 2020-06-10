## TemplatePatternDemon

```java
package com.demon;

public class TemplatePatternDemon {
    public static void main(String[] args) {

        User teacher = new Teacher();
        teacher.createUser();

        User student = new Student();
        student.createUser();
    }
}

abstract class User {
    void createUser() {
        validateParams();
        buildRequest();
        create();
        buildResponse();
	}

    private void validateParams() {
        System.out.println("validate params...");
    }

    private void buildRequest() {
        System.out.println("build request...");
    }

    protected abstract void create();

    private void buildResponse() {
        System.out.println("build response...\n=================");
    }
}

class Teacher extends User {
    @Override
    protected void create() {
        System.out.println("create teacher logic...");
    }
}

class Student extends User {
    @Override
    protected void create() {
        System.out.println("create student logic...");
    }
}
```
