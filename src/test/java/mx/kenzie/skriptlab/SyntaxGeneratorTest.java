package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.internal.GeneratedCondition;
import mx.kenzie.skriptlab.internal.GeneratedEffect;
import mx.kenzie.skriptlab.internal.GeneratedExpression;
import mx.kenzie.skriptlab.internal.GeneratedPropertyCondition;
import org.junit.Test;

@SuppressWarnings({"unchecked", "RawUseOfParameterized"})
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
    
    @Test
    public void createPropertyCondition() throws Exception {
        final SyntaxGenerator generator = new SyntaxGenerator();
        final Syntax syntax = generator.createPropertyCondition(String::isBlank, String.class, "blank");
        assert syntax.type().getSimpleName().equals("GeneratedCondition1");
        assert GeneratedPropertyCondition.class.isAssignableFrom(syntax.type());
        assert syntax.patterns().length == 2;
        assert syntax.type().getConstructor().newInstance() instanceof GeneratedPropertyCondition;
    }
    
    @Test
    public void createExpression() throws Exception {
        final SyntaxGenerator generator = new SyntaxGenerator();
        final Syntax syntax = generator.createExpression(Long.class, (event, inputs) -> System.currentTimeMillis(),
            "[the] current time [in millis[econds]]");
        assert syntax.type().getSimpleName().equals("GeneratedExpression1");
        assert GeneratedExpression.class.isAssignableFrom(syntax.type());
        assert syntax.patterns().length == 1;
        assert syntax.type().getConstructor().newInstance() instanceof GeneratedExpression;
    }
    
}
