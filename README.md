SkriptLab
=====

An automatic syntax-creation library for Skript addons.

### Introduction

The original version of this resource was an experiment in my Mask library to be able to auto-generate Skript syntax at
runtime using only annotations.

Skript's syntax classes require a large amount of boilerplate that, for the majority of cases, ends up being
cookie-cutter work, which is slow, tedious and largely unnecessary for creating simple wrappers.
This will deter many plugins from adding their own native Skript support, leading to the creation of addons dedicated
entirely to providing syntax for one or two plugins, or a group of similar plugins. (See regions addon, permissions
addon, holograms addon, etc.)

In an ideal world, it would be up to the plugin creator to add their own Skript support, but it is very understandable
why they would not want to.

SkriptLab aims to solve this through the following goals:

- To provide a simple and minimalist way to add Skript syntax
- To generate syntax that is comparable in speed and quality to hand-written syntax classes
- To provide a lightweight dependency
- To avoid excess garbage and memory pollution

Every Skript syntax requires its own class. This makes anonymous runtime generation problematic, since the programmer
might not know what classes are required.

To overcome this problem, SkriptLab compiles individual syntax classes to suit the program's needs while it is running.
These are assigned to a child classloader, and can be disposed of or replaced as needed.

### Maven Information

```xml

<repository>
    <id>kenzie</id>
    <url>https://repo.kenzie.mx/releases</url>
</repository>
``` 

```xml

<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>skriptlab</artifactId>
    <version>2.0.0</version>
    <scope>compile</scope>
</dependency>
```

## Usage

### Syntax Generation

Syntax is created through a `SyntaxGenerator`. This is a reusable controller object that holds on to all the generated
syntax information.
This also holds the class loader in which syntax elements are bootstrapped, so once the syntax generator object is
discarded the syntax classes can also be garbage collected (providing they are unregistered and no longer in use).

The generator has two uses:

To make full syntax classes out of patterns and simple functional interfaces.

```java 
final Syntax syntax = generator.createCondition((event, inputs) -> {
    final String text = inputs.get(0);
    return text.isBlank();
}, "%text% is blank");
// syntax.register();
```

To generate syntax classes from annotations on members.

```java 

@Type
public class Elephant { // %elephant%
    
    @Expression("[a] new elephant")
    public static Elephant create() { // set {_var} to a new elephant
        return new Elephant();
    }
    
    @PropertyCondition
    public boolean isOkay() { // if {_var} is okay:
        return true;
    }
    
    @PropertyExpression
    public double trunkLength() { // {_var}'s trunk length
        return 5;
    }
    
    @PropertyExpression(mode = AccessMode.SET)
    public void trunkLength(Number length) { // set {_var}'s trunk length to 10
        // ...
    }
    
    @PropertyExpression(mode = AccessMode.ADD, value = "trunk length")
    public void trunkLength(Number length) { // add 2 to {_var}'s trunk length
        // ...
    }

}
```

### Annotations

SkriptLab is accessed through a series of five basic annotations.

| Name               | Target                | Usage                                                                                                                                                     |
|--------------------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Effect             | Method                | A runnable Skript effect. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object.           |
| Condition          | Method (boolean)      | A condition. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object.                        |
| PropertyCondition  | Method (boolean)      | A shortcut for creating simple conditions of the type `X is alive`                                                                                        |
| Expression         | Method (any non-null) | An expression that returns a value. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object. |
| PropertyExpression | Methods               | An expression of the type `the X of Y` or `Y's X` where X is the property name and Y is the object. Multiple methods can be used for each access mode.    |
| Type               | Class                 | Registers the given object class as a Skript Type, allowing it to be used in syntax.                                                                      |

These annotations are designed to be unobtrusive and easy to add into a plugin's existing code without requiring any
changes or extra classes to be created.

SkriptLab will attempt to generate sensible and legible syntax automatically based on the method/class name, but
this can be manually overridden (such as when inputs are required) with the annotation value.
