package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.internal.GeneratedCondition;
import mx.kenzie.skriptlab.internal.GeneratedEffect;
import org.junit.Test;

public class SyntaxGeneratorTest {
    
    @Test
    public void createEffect() throws Exception {
        final SyntaxGenerator generator = new SyntaxGenerator();
        final Syntax syntax = generator.createEffect((event, inputs) -> {
            final String text = inputs.get(0);
            System.out.println(text);
        }, "print %text%");
        assert syntax.type().getSimpleName().equals("GeneratedEffect1");
        assert GeneratedEffect.class.isAssignableFrom(syntax.type());
        assert syntax.patterns().length == 1;
        assert syntax.type().getConstructor().newInstance() instanceof GeneratedEffect;
    }
    
    @Test
    public void createCondition() throws Exception {
        final SyntaxGenerator generator = new SyntaxGenerator();
        final Syntax syntax = generator.createCondition((event, inputs) -> {
            final String text = inputs.get(0);
            return text.isBlank();
        }, "%text% is blank");
        assert syntax.type().getSimpleName().equals("GeneratedCondition1");
        assert GeneratedCondition.class.isAssignableFrom(syntax.type());
        assert syntax.patterns().length == 1;
        assert syntax.type().getConstructor().newInstance() instanceof GeneratedCondition;
    }
    
}
