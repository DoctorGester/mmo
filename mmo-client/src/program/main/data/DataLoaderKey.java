package program.main.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataLoaderKey {
    String value() default "";
	String function() default "";
	Class<? extends Enum> dataEnum() default Enum.class;
}
