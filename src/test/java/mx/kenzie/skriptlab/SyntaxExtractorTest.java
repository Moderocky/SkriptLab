package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Condition;
import mx.kenzie.skriptlab.annotation.Effect;
import mx.kenzie.skriptlab.error.PatternCompatibilityException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SyntaxExtractorTest {
    
    @Condition
    public static void broken() { // for making sure this isn't a valid condition
    }
    
    @Test
    public void prepare() {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        assert extractor.syntax == null;
        assert extractor.effects == null;
        extractor.prepare(Dummy.class);
        assert extractor.syntax != null;
        assert extractor.effects != null;
    }
    
    @Test
    public void divine() {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        extractor.prepare(Dummy.class);
        assert extractor.syntax.isEmpty();
        assert extractor.effects.isEmpty();
        extractor.divine();
        assert !extractor.syntax.isEmpty();
        assert extractor.effects.size() == 2;
        assert extractor.conditions.size() == 1;
    }
    
    @Test
    public void makeSyntax() {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        extractor.prepare(Dummy.class);
        extractor.divine();
        final List<Syntax<?>> list = new ArrayList<>();
        extractor.makeSyntax(list);
    }
    
    @Test
    public void makeSyntaxSimple() throws Exception {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        extractor.prepare(Dummy.class);
        extractor.divineEffect(Dummy.class.getMethod("test"));
        assert extractor.syntax.size() == 1;
    }
    
    @Test
    public void makeSyntaxCheckName() throws Exception {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        extractor.prepare(Dummy.class);
        extractor.divineCondition(Dummy.class.getMethod("isItWorking"));
        assert extractor.syntax.size() == 1;
        final SyntaxExtractor.MaybeSyntax maybe = extractor.syntax.iterator().next();
        maybe.verify();
        final Syntax<?> syntax = maybe.generate();
        assert syntax.patterns().length == 1;
        assert syntax.patterns()[0].equals("is it working");
    }
    
    @Test(expected = PatternCompatibilityException.class)
    public void makeConditionNoBooleanReturn() throws Exception {
        final SyntaxExtractor extractor = new SyntaxExtractor(new SyntaxGenerator());
        extractor.prepare(Dummy.class);
        extractor.divineCondition(SyntaxExtractorTest.class.getMethod("broken"));
        assert extractor.syntax.size() == 1;
    }
    
    public static class Dummy {
        
        @Effect("print %string%")
        public static void print(String string) {
            System.out.println(string);
        }
        
        @Condition
        public static boolean isItWorking() {
            return true;
        }
        
        @Effect("test %object%")
        public void test() {
            System.out.println("test");
        }
        
    }
    
}
