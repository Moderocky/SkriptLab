package mx.kenzie.skriptlab;

import org.junit.Test;

public class PatternDigestTest {
    
    @Test
    public void interpolationsBasic() {
        final Object[] objects = PatternDigest.interpolations("test");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsOptionalStart() {
        final Object[] objects = PatternDigest.interpolations("[the] test");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsOptionalEnd() {
        final Object[] objects = PatternDigest.interpolations("[the] test[er]");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsOptionalMiddle() {
        final Object[] objects = PatternDigest.interpolations("te[submarine]st");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsOptionalSplit() {
        final Object[] objects = PatternDigest.interpolations("[the] test[(er|ing)]");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsSplitOptionalEmpty() {
        final Object[] objects = PatternDigest.interpolations("[the] test([ers]|ing)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsSplitBasic() {
        final Object[] objects = PatternDigest.interpolations("[the] test(er|ing)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("tester") : objects[0];
    }
    
    @Test
    public void interpolationsSplitOptional() {
        final Object[] objects = PatternDigest.interpolations("[the] test(er[s]|ing)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("tester") : objects[0];
    }
    
    @Test
    public void interpolationsSplitOwnWord() {
        final Object[] objects = PatternDigest.interpolations("[the] test (er|ing)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test er") : objects[0];
    }
    
    @Test
    public void interpolationsSplitEmpty() {
        final Object[] objects = PatternDigest.interpolations("[the] test(|ing)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test") : objects[0];
    }
    
    @Test
    public void interpolationsSplitEmptyOtherHalf() {
        final Object[] objects = PatternDigest.interpolations("[the] test(ing|)");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("testing") : objects[0];
    }
    
    @Test
    public void interpolationsInput() {
        final Object[] objects = PatternDigest.interpolations("[the] test %blob%");
        assert objects.length == 2 : objects.length;
        assert objects[0].equals("test ") : objects[0];
        assert objects[1] instanceof PatternDigest.Input;
    }
    
    @Test
    public void interpolationsInputNoGap() {
        final Object[] objects = PatternDigest.interpolations("[the] test%blob%");
        assert objects.length == 2 : objects.length;
        assert objects[0].equals("test") : objects[0];
        assert objects[1] instanceof PatternDigest.Input;
    }
    
    @Test
    public void interpolationsSplitInput() {
        final Object[] objects = PatternDigest.interpolations("[the] test (%blob%|foo)");
        assert objects.length == 2 : objects.length;
        assert objects[0].equals("test ") : objects[0];
        assert objects[1] instanceof PatternDigest.Input;
    }
    
    @Test
    public void interpolationsSplitTextInput() {
        final Object[] objects = PatternDigest.interpolations("[the] test(ing %blob%|er)");
        assert objects.length == 2 : objects.length;
        assert objects[0].equals("testing ") : objects[0];
        assert objects[1] instanceof PatternDigest.Input;
    }
    
    @Test
    public void interpolationsOptionalInput() {
        final Object[] objects = PatternDigest.interpolations("[the] test [%blob%]");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test ") : objects[0];
    }
    
    @Test
    public void interpolationsOptionalInputText() {
        final Object[] objects = PatternDigest.interpolations("[the] test [%blob%] thing");
        assert objects.length == 1 : objects.length;
        assert objects[0].equals("test thing") : objects[0];
    }
    
    @Test
    public void digest() {
        final PatternDigest digest = new PatternDigest("[the] test");
        digest.digest();
        final String result = digest.example();
        assert result != null;
        assert result.equals("test") : result;
    }
    
    @Test
    public void example() {
        final PatternDigest digest = new PatternDigest("[the] test ");
        digest.digest();
        final String result = digest.example();
        assert result != null;
        assert result.equals("test") : result;
    }
    
}
