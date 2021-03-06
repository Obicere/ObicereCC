package org.obicere.cc.projects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RunnerManifest {

    public String author();

    public double version();

    public int difficulty();

    public String description() default "";

}
