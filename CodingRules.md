# Java Coding Rules

## Naming
***1. Names representing packages should be in all lower case.***
```
    com.company.application.ui
```

***2. Class/enum names must be nouns and written in PascalCase.***
```
    Line, AudioSystem
```
***3. Variable names must be in camelCase.***
```
    line, audioSystem
```

***4. Constant names must be all uppercase using underscore to separate words.***
```
    MAX_ITERATIONS, COLOR_RED
```

***5. Names representing methods must be verbs and written in camelCase.***
```
    getName(), computeTotalWidth()
```

***6.Abbreviations and acronyms should not be uppercase when used as a part of a name.***

**Good**
```
    exportHtmlSource();
    openDvdPlayer();
```
**Not Good**
```
    exportHTMLSource();
    openDVDPlayer();
```

***7. All names should be written in English.***

***8. Boolean variables/methods should be named to sound like booleans.***
```
    //variables
    isSet, isVisible, isFinished, isFound, isOpen, hasData, wasOpen
    
    //methods
    boolean hasLicense();
    boolean canEvaluate();
    boolean shouldAbort = false;
```

***9. Plural form should be used on names representing a collection of objects***
```
    Collection<Point> points;
    int[] values;
```

***10. Iterator variables can be called i, j, k etc.***
```
    for (Iterator i = points.iterator(); i.hasNext(); ) {
        ...
    }
    
    for (int i = 0; i < nTables; i++) {
        ...
    }
```

***11. Associated constants should have a common prefix***
```
    static final int COLOR_RED   = 1;
    static final int COLOR_GREEN = 2;
    static final int COLOR_BLUE  = 3;
```

## Layouts
***1. Line length should be no longer than 120 chars, improve readability***
```
    setText("Long line split"
            + "into two parts.");
    if (isReady) {
        setText("Long line split"
                + "into two parts.");
    }
```
***2. Use K&R style brackets***

**Good**
```
    while (!done) {
        doSomething();
        done = moreToDo();
    }
    
    public void someMethod() throws SomeException {
        ...
    }
    
    if (condition) {
        statements;
    } else if (condition) {
        statements;
    } else {
        statements;
    }
    
    for (initialization; condition; update) {
        statements;
    }
    
    switch (condition) {
        case ABC:
            statements;
            // Fallthrough
        case DEF:
            statements;
            break;
        case XYZ:
            statements;
            break;
        default:
            statements;
            break;
    }
    
    try {
        statements;
    } catch (Exception exception) {
        statements;
    } finally {
        statements;
    }
    
```
**Bad**
```
    while (!done)
    {
        doSomething();
        done = moreToDo();
    }
```

# Comments
***1. All comments should be written in English.***
***2. Write descriptive header comments for all public classes/methods***
```
    /**
     * Returns lateral location of the specified position.
     * If the position is unset, NaN is returned.
     *
     * @param x X coordinate of position.
     * @param y Y coordinate of position.
     * @param zone Zone of position.
     * @return Lateral location.
     * @throws IllegalArgumentException  If zone is <= 0.
     */
    public double computeLocation(double x, double y, int zone)
            throws IllegalArgumentException {
        //...
    }
```

***3. One line comment.***
```java
    // This is one line comment
```

***4. Multi lines comments***
```java
    /*
        line 1
        line 2
     */
```

***5. Must have comments at beginning of complex logic.***

**Good**
```
    public void functionA() {
        // logic to ....
        logicA();
    }
```

**Bad**
```
    public void functionA() {
        logicA();
        // logic to ....
    }
```







