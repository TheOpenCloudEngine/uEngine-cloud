## Local test demo - eureka server,client,zuul

## Start eureka server

When you clone gitlab repository, use 'root' / 'adminadmin' as username and password for console. 

```
git clone http://gitlab.uengine.io/theopencloudengine/uengine-eureka-server

cd uengine-eureka-server
mvn package

java -jar target/uengine-eureka-server-1.0-SNAPSHOT.jar

```

Check out eureka server url [http://localhost:8761/](http://localhost:8761/)

## Start zuul server

```
git clone http://gitlab.uengine.io/theopencloudengine/uengine-eureka-zuul

cd uengine-eureka-zuul
mvn package

java -jar target/uengine-eureka-zuul-1.0-SNAPSHOT.jar
```

Check out "UENGINE-EUREKA-ZUUL" is up on eureka server

## Start eureka client sample application

Launch 2 demo application, each env DEPLOYMENT=BLUE,DEPLOYMENT=GREEN 

```
git clone http://gitlab.uengine.io/theopencloudengine/uengine-eureka-client

cd uengine-eureka-client
mvn package

java -jar target/uengine-eureka-client-1.0-SNAPSHOT.jar --DEPLOYMENT=BLUE --server.port=9091

java -jar target/uengine-eureka-client-1.0-SNAPSHOT.jar --DEPLOYMENT=GREEN --server.port=9092
```

Check out "UENGINE-EUREKA-CLIENT" is up on eureka server, with 2 instances.

Check out [http://localhost:9091/](http://localhost:9091/) and message is

```
I am BLUE, instanceId:bagseunpiluimbp:uengine-eureka-client:9091
```

Check out [http://localhost:9092/](http://localhost:9092/) and message is

```
I am GREEN, instanceId:bagseunpiluimbp:uengine-eureka-client:9092
```

## Test BLUE - GREEN switch

Set prod.uengine.io,dev.uengine.io as 'localhost' in /etc/hosts file

```
vi /etc/hosts

127.0.0.1       localhost       prod.uengine.io   dev.uengine.io
```

On uengine-eureka-zuul directory, run script 

```
cd uengine-eureka-zuul
sh proxy-interval-test.sh 

I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
.
.
.
```

This script makes a get request to the zuul server to the prod.uengine.io domain. 

And by default, zuul server will proxy the prod.uengine.io to the instance with the value of the BLUE environment variable.

You can switch the BLUE and GREEN status of eureka service through the following rest api.

```
curl -X POST \
  http://localhost:4000/service \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "service": "uengine-eureka-client",
  "prod": "GREEN",
  "dev": "BLUE"
}'
```


Immediately after the change, the proxy-interval-test.sh file will log as follows:

```
I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
I am BLUE,   instanceId:bagseunpiluimbp:uengine-eureka-client:9091
I am GREEN,   instanceId:bagseunpiluimbp:uengine-eureka-client:9092
I am GREEN,   instanceId:bagseunpiluimbp:uengine-eureka-client:9092
I am GREEN,   instanceId:bagseunpiluimbp:uengine-eureka-client:9092
.
.
.
```


## Test Circuit - Trubine Monitor

Checkout [http://localhost:4000/hystrix](http://localhost:4000/hystrix)

Insert "http://localhost:4000/turbine.stream" at stream url, and click "Monitor stream"

You can see circuit monitoring dashboard per each hystrix command method.

In dashboard, There is one hystrix command "getHome" in "HomeService" pool 

<br>

In uengine-eureka-client, Application start with requestMapping "/" 

See Application.java

```
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableEurekaClient
@RestController
@EnableCircuitBreaker
@EnableHystrixDashboard
public class Application {

    @Autowired
    private Environment environment;

 
    @Autowired
    private HomeService homeService;

  
    @RequestMapping("/")
    public String home() throws Exception {
        return homeService.getHome();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

}
```

And HomeService is a component witch has a 'getHome' method, annotated with '@HystrixCommand'

'getHome' method work.. 

 1) get self eureka instance infomation from eureka server
 2) get instance metadata value "deployment"
 3) Print "deployment" and "instanceId"

```
@Component
public class HomeService {

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private Environment environment;

    @HystrixCommand
    public String getHome() {
        InstanceInfo info = discoveryClient.getApplicationInfoManager().getInfo();
        String deployment = "";
        if (info.getMetadata().get("deployment") != null) {
            deployment = info.getMetadata().get("deployment").toString();
        }
        return "I am " + deployment + ",   instanceId:" + info.getInstanceId();
    }
}
```

The instance metadata of the Eureka client is registered with the Eureka server by the application.yml setting at the time the client is booted.

See  uengine-eureka-client/src/main/resources/application.yml


```
spring:
  application:
      name: uengine-eureka-client
  profiles:
    active: "dev"

---
spring:
  profiles: dev

server:
  port: 8080
  servletPath: /

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    healthcheck:
      enabled: true
  instance:
    statusPageUrlPath: ${server.servletPath}info
    healthCheckUrlPath: ${server.servletPath}health
    metadataMap:
        deployment: ${DEPLOYMENT}
```

'eureka.client.serviceUrl.defaultZone' is eureka server url.

And eureka.instance.metadataMap.deployment takes value from environment "${DEPLOYMENT}". 

So let's remember that when we run the Eureka client, we run it with the following environment variables:
  
```
java -jar target/uengine-eureka-client-1.0-SNAPSHOT.jar --DEPLOYMENT=BLUE --server.port=9091

java -jar target/uengine-eureka-client-1.0-SNAPSHOT.jar --DEPLOYMENT=GREEN --server.port=9092
```


