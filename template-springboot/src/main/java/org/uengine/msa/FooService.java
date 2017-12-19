package org.uengine.msa;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.stereotype.Component;

/**
 * Created by uengine on 2017. 10. 6..
 */
@Component
public class FooService {

    @HystrixCommand(fallbackMethod = "defaultFoo")
    public Foo getFoo(String id) throws Exception{
        //do stuff that might fail
        Foo foo = new Foo();
        foo.setId(id);
        foo.setName("getFoo");
        return foo;
    }

    @HystrixCommand(fallbackMethod = "defaultFoo")
    public Foo updateFoo(String id) {
        //do stuff that might fail
        Foo foo = new Foo();
        foo.setId(id);
        foo.setName("updateFoo");
        return foo;
    }

    public Foo defaultFoo(String id) {
        Foo foo = new Foo();
        foo.setId(id);
        foo.setName("defaultFoo");
        return foo;
    }
}
