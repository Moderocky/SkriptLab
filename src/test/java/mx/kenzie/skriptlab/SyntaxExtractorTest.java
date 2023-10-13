package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Effect;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SyntaxExtractorTest {
    
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
    
    public static class Dummy {
        
        @Effect
        public static void test() {
            System.out.println("test");
        }
        
        @Effect("print %string%")
        public static void print(String string) {
            System.out.println(string);
        }
        
    }
    
}
