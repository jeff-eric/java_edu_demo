package edu.jphoebe.demo.springmvc.framework.annotation;

import java.lang.annotation.*;

/**
 * JPController class
 *
 * @author 蒋时华
 * @date 2017/12/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface JPController {

    String value() default "";

}
