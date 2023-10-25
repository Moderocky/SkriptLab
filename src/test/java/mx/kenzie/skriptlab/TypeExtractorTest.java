package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Type;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypeExtractorTest {
    
    @Test
    public void divine() {
        final TypeExtractor extractor = new TypeExtractor(new SyntaxGenerator());
        assert extractor.found == null;
        extractor.prepare(TypeExtractorTest.class);
        assert extractor.found != null;
        assert extractor.found.isEmpty();
        extractor.divine();
        assert extractor.found != null;
        assert extractor.found.size() == 3;
        assert extractor.found.contains(DummyA.class);
        assert extractor.found.contains(DummyB.class);
        assert extractor.found.contains(DummyC.class);
        assert !extractor.found.contains(DummyX.class);
    }
    
    @Test
    public void collect() {
        final TypeExtractor extractor = new TypeExtractor(new SyntaxGenerator());
        final List<Registered> list = new ArrayList<>();
        assert extractor.found == null;
        extractor.prepare(DummyA.class);
        assert extractor.found != null;
        assert extractor.found.isEmpty();
        extractor.divine();
        assert extractor.found != null;
        extractor.collect(list);
        assert !list.isEmpty();
        assert list.size() == 1 : list.size();
        final TypeInfo a = (TypeInfo) list.get(0);
        assert a != null;
        assert a.type() == DummyA.class : a.type();
        assert Objects.equals(a.name(), "dummya") : a.name();
        assert a.patterns().length == 1 : a.patterns().length;
        assert a.patterns()[0].equals("dummy a") : a.patterns()[0];
        list.clear();
        extractor.prepare(DummyB.class);
        assert extractor.found != null;
        assert extractor.found.isEmpty();
        extractor.divine();
        assert extractor.found != null;
        extractor.collect(list);
        assert !list.isEmpty();
        assert list.size() == 1 : list.size();
        final TypeInfo b = (TypeInfo) list.get(0);
        assert b != null;
        assert b.type() == DummyB.class : b.type();
        assert Objects.equals(b.name(), "dummyb") : b.name();
        assert b.patterns().length == 1 : b.patterns().length;
        assert b.patterns()[0].equals("thing") : b.patterns()[0];
        list.clear();
        extractor.prepare(DummyC.class);
        assert extractor.found != null;
        assert extractor.found.isEmpty();
        extractor.divine();
        assert extractor.found != null;
        extractor.collect(list);
        assert !list.isEmpty();
        assert list.size() == 1 : list.size();
        final TypeInfo c = (TypeInfo) list.get(0);
        assert c != null;
        assert c.type() == DummyC.class : c.type();
        assert Objects.equals(c.name(), "blob") : c.name();
        assert c.patterns().length == 1 : c.patterns().length;
        assert c.patterns()[0].equals("dummy c") : c.patterns()[0];
        list.clear();
    }
    
    @Test
    public void checkClass() {
        final TypeExtractor extractor = new TypeExtractor(new SyntaxGenerator());
        final List<Class<?>> list = new ArrayList<>();
        extractor.checkClass(DummyA.class, list);
        assert list.size() == 1;
        extractor.checkClass(DummyX.class, list);
        assert list.size() == 1;
        extractor.checkClass(DummyA.class, list);
        assert list.size() == 2;
    }
    
    @Type
    public static class DummyA {
    
    }
    
    @Type("thing")
    public static class DummyB {
    
    }
    
    @Type(codeName = "blob")
    public static class DummyC {
    
    }
    
    public static class DummyX {
    
    }
    
}
