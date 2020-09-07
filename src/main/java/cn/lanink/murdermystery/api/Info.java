package cn.lanink.murdermystery.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lt_name
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD,
        ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PACKAGE,
        ElementType.LOCAL_VARIABLE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
        ElementType.PARAMETER})
public @interface Info {

    String value();

}
