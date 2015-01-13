package core.handlers.terminal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CommandArgument {
	boolean mandatory() default true;
	String name() default "";
	int arity() default 0;
	boolean variableArity() default false;
}
