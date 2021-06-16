SkriptLab
=====

An automatic syntax-creation library for Skript addons.

### Introduction

The original version of this resource was an experiment in my Mask library to be able to auto-generate Skript syntax at runtime using only annotations.

Skript's syntax classes require a large amount of boilerplate that, for the majority of cases, ends up being cookie-cutter work, which is slow, tedious and largely unnecessary for creating simple wrappers.
This will deter many plugins from adding their own native Skript support, leading to the creation of addons dedicated entirely to providing syntax for one or two plugins, or a group of similar plugins. (See regions addon, permissions addon, holograms addon, etc.)

In an ideal world, it would be up to the plugin creator to add their own Skript support, but it is very understandable why they would not want to.

SkriptLab aims to solve this through the following goals:
 - To provide a simple and minimalist way to add Skript syntax
 - To generate syntax that is comparable in speed and quality to hand-written syntax classes
 - To provide a lightweight dependency
 - To avoid excess garbage and memory pollution

### Maven Information

```xml
<repository>
    <id>pan-repo</id>
    <name>Pandaemonium Repository</name>
    <url>https://gitlab.com/api/v4/projects/18568066/packages/maven</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>skriptlab</artifactId>
    <version>1.0.2</version>
    <scope>compile</scope>
</dependency>
```

### Usage

SkriptLab is accessed through a series of five basic annotations.

|Name|Target|Usage|
|----|------|-----|
|Effect|Method (void)|A runnable Skript effect. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object.|
|Condition|Method (boolean)|A condition. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object.|
|Expression|Method (any non-null)|An expression that returns a value. Inputs are passed to the method parameters. If used on a dynamic method, the first input is assumed to be the object.|
|Property|Field (any)|An expression of the type `the X of Y` or `Y's X` where X is the property name and Y is the object. This may be used only on dynamic fields. Primitives and number types are automatically wrapped.|
|SkriptType|Class|Registers the given object class as a Skript Type, allowing it to be used in syntax.|

The `Documentation` annotation can be used in conjunction with `SkriptType` to provide extra details for Skript's automatic user documentation generation. This is not necessary, and the type alone is all that is required for simple registration. 

These annotations are designed to be unobtrusive and easy to add into a plugin's existing code without requiring any changes or extra classes to be created.

SkriptLab will attempt to generate sensible and legible syntax automatically based on the method/field/class name, but this can be manually overridden (such as when inputs are required) with the annotation value.

A very basic example of use can be seen below.

```java
@SkriptType("elephant")
public class Elephant {
    
    @Expression("[a] new elephant")
    public static Elephant create() {
        return new Elephant();
    }
    
    @Property("abcd")
    private double value = 10.0;
    
    @Condition
    boolean isChonky() { return true; }
    
    @Condition
    public boolean hasEars() { return true; }
    
    @Effect("make %elephant% eat leaves")
    public void eat() {
        System.out.println("I'm eating leaves :)");
    }
}
```

This would automatically generate the following:
 - Expression `[a] new elephant`
 - Property `([the] abcd of %elephant%|%elephant%'s abcd)`
 - Condition `%elephant% is chonky` (and the inverse)
 - Condition `%elephant% has ears` (and the inverse)
 - Effect `make %elephant% eat leaves`

In order to register the syntax, your plugin may keep a `SyntaxGenerator` instance. This is best done in your plugin class (for easy access and disposal) but can be done anywhere.

A very simple example is below.

```java
public class ExamplePlugin extends JavaPlugin {
    private SyntaxGenerator generator;
    
    @Override
    public void onEnable() {
        generator = new SyntaxGenerator(this);
        generator.registerSyntaxFrom(MyClass.class, Elephant.class, AnotherClass.class);
    }
    
    @Override
    public void onDisable() {
        generator = null;
    }
}
```

The `SyntaxGenerator#registerSyntaxFrom` method accepts an array of classes, and will automatically scour them for annotations and deal with them.
If Skript is not on the server (or otherwise non-functional) the `new SyntaxGenerator` constructor will throw an `IllegalStateException`.

The registration method can throw exceptions with error details if a problem was encountered while creating or loading the syntax classes.

**Note:** It is very important to annul all instances of your `SyntaxGenerator` once your plugin is disabled. This will automatically remove the generated classes from memory to prevent a memory leak and pollution - if you want to get rid of your SyntaxGenerator instance as soon as registration is done this should also be fine as Skript will keep the individual syntax classes in memory for you.

Generated syntax classes cannot be recycled currently.

### Design Choices

The original version of SkriptLab (lovingly named `skript-experiment-3`) used a system of local classes and abstraction to create the syntax classes.

While this was easier, in the sense that all code was present at compile-time, it caused some issues with registration as the constructors of local classes have several oddities and are in a strange state between public and explicitly private, which caused several headaches.

Instead of this approach, SkriptLab writes the bytecode for the syntax classes at runtime and loads them using a custom classloader which is a child of your plugin's classloader for accessibility.
This approach is significantly more complicated but has the advantage of allowing easier control and customisation of individual classes where necessary.

The bytecode is stripped of all labels, comments and identifiers to minify it as much as possible, though the result may be extractable through a decompiler.

Using compile-time annotations to explicitly generate the classes was considered as an alternative option, but I decided against this approach because I felt it would compromise the integrity of plugins by adding extra classes and had a higher potential for error.

### Best Practices

Below are detailed some of the best practices for using SkriptLab in the most efficient manner. Note: using SkriptLab in other ways is absolutely fine, but these tips will minimise overhead.

The `@Condition` annotation is fastest on public primitive (boolean) members. If placed on a `public boolean` method or field, the generated bytecode will use a direct access which takes nanoseconds, whereas if used on a non-public boolean field or a non-primitive `Boolean` type it must use reflection under the hood. The finality of the member does not matter.

The `@Effect` annotation is fastest when used on a public method. When used on a dynamic method, the first `%type%` input of the effect is assumed to be the object on which to call the method. A regular accessor is generated for all possible cases.

Using the `@EventValue` annotation is only possible on public dynamic methods. It may be possible on primitive methods but this can cause issues with the generated `Getter<V,E>` generic typing (since primitive generics are technically illegal!)
