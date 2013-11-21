package se.su.it.svc.aspect

import java.lang.annotation.*

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AuditHideReturnValue {
}
