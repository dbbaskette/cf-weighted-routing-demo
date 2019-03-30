Weighted Routing and Multi-Port Demo
===

This is a simple application to demo weighted routing and multi-port support in PCF 2.5.

The Application
---

The app is straightforward, it simply displays a logo and a hit counter. This logo can be controlled with the `DEMO_LOGO` environment variable, with the following valid values:

- `emacs`
- `vim`

If the `DEMO_LOGO` environment variable does not exist, or is anything other than the above, it will default to `emacs`.

The app has three endpoints:

- `/` - Logo and Hit Counter
- `/env` - Lists all environment variables and JVM version
- `/reset` - Reset the hit counter

The app also has Spring Boot Actuator running on a separate port, port 8081 by default.

The code can be built as a standard Maven project:

```
./mvnw clean package
```

And to make the demo a bit easier to copy and paste, let's store the default domain in an environment variable:

```
export CF_DOMAIN=`cf curl /v2/domains | jq .resources[0].entity.name | tr -d '"'`
```

Weighted Routing
===

To begin, let's start by pushing up two versions of our application, each with their own default route:

```
cf push editor-v1 -p target/cf-routing-demo-0.0.1-SNAPSHOT.jar
```

```
cf push editor-v2 -p target/cf-routing-demo-0.0.1-SNAPSHOT.jar --no-start

cf set-env editor-v2 DEMO_LOGO vim

cf start editor-v2
```

By default, CF will create a second domain for the Istio router named "mesh.<DOMAIN>", where DOMAIN is our default application domain. Let's go ahead and create a route using this domain to v1 of our application:

```
cf map-route editor-v1 mesh.$CF_DOMAIN --hostname editor
```

With our Istio route mapped to v1, let's give it a weight of 9 before we map it to v2 of our application. By default, all routes have a weight of 1, so by setting this we'll send 90% of the traffic to v1 and 10% to v2. Additionally, as this feature is still in beta, we'll interact with the API directly to do this. You'll notice a number of in-line commands here:

```
cf curl /v3/route_mappings/$(echo "$(cf curl /v3/apps/$(cf app editor-v1 --guid)/route_mappings | jq .resources[1].guid)" | tr -d '"') -X PATCH -d '{"weight": 9}'
```

These in-line commands look up both the application GUID and the route GUID for our Istio route, and then call the /v3/route_mappings API call, setting "weight" to "9".

Next, we can map this same Istio route to v2 just as we did with v1:

```
cf map-route editor-v2 mesh.$CF_DOMAIN --hostname editor
```

It may take a few seconds to propagate, but with this, we're now splitting our traffic 90/10 between v1 and v2! If we'd like to shift this balance to send more to v2, we can make the same API call:

```
cf curl /v3/route_mappings/$(echo "$(cf curl /v3/apps/$(cf app editor-v2 --guid)/route_mappings | jq .resources[1].guid)" | tr -d '"') -X PATCH -d '{"weight": 27}'
```

Again, this will take a few seconds to propagate, but by giving v2 a weight of 27, compared to v1's weight if 9, v2 will receive 75% of the traffic while v1 receives 25%.

Multi-Port Support
===

As mentioned, we have Spring Boot Actuator running on a separate port, but the CF API now allows us to configure our applications to listen on multiple port. Let's push up a separate version of our application for purposes of our demo:

```
cf push multiport -p target/cf-routing-demo-0.0.1-SNAPSHOT.jar
```

Next, we'll tell CF about the ports that our application is listening on. In this case, it listens on port 8080 (main application) and port 8081 (Spring Boot Actuator):

```
cf curl /v2/apps/$(cf app multiport --guid) -X PUT -d '{"ports": [8080, 8081]}'
```

We then need to prepare a route that we'll use to map to our apps second port. In this case, we'll give it the hostname "actuator":

```
cf create-route dev $CF_DOMAIN --hostname actuator
```

Finally, we'll map our new route to port 8081 of our application:

```
cf curl /v2/route_mappings -X POST -d "{\"app_guid\": \"$(cf app multiport --guid)\", \"route_guid\": \"$(cf curl /v2/routes?q=host:actuator | jq .resources[0].metadata.guid | tr -d '"')\", \"app_port\": 8081}"
```


Reset Demo
===

```
cf delete editor-v1 -f

cf delete editor-v2 -f

cf delete multiport -f

cf delete-route $CF_DOMAIN --hostname editor-v1 -f

cf delete-route $CF_DOMAIN --hostname editor-v2 -f

cf delete-route $CF_DOMAIN --hostname multiport -f

cf delete-route mesh.$CF_DOMAIN --hostname editor -f

cf delete-route $CF_DOMAIN --hostname actuator -f
```