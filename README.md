# charybdis

Provides fixed endpoints of M3 annotation data used in publication. This allows reliable URL that can be published along with papers.


## Build and run

With JDK21+
```bash
mvn package
java -jar target/charybdis.jar
```

## Exercise the application

```
curl -X GET http://localhost:8080/n0
```

## Try health and metrics

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .

```

## Build the Docker Image

```
# We need our github access key to get VARS dependencies
cp ~/.m2/settings.xml . 
docker build -t mbari/charybdis .
```

## Start the application with Docker

```
docker run --rm -p 8080:8080 charybdis:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                # Verify which cluster
kubectl get pods                    # Verify connectivity to cluster
kubectl create -f app.yaml   # Deply application
kubectl get service charybdis  # Get service info
```

## Native image with GraalVM

GraalVM allows you to compile your programs ahead-of-time into a native
 executable. See https://www.graalvm.org/docs/reference-manual/aot-compilation/
 for more information.

You can build a native executable in 2 different ways:
* With a local installation of GraalVM
* Using Docker

### Local build

Download Graal VM at https://github.com/oracle/graal/releases, the versions
 currently supported for Helidon is `19.1.1` and `19.2.0`.

```
# Setup the environment
export GRAALVM_HOME=/path
# build the native executable
mvn package -Pnative-image
```

You can also put the Graal VM `bin` directory in your PATH, or pass
 `-DgraalVMHome=/path` to the Maven command.

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin
 for more information.

Start the application:

```
./target/charybdis
```

### Multi-stage Docker build

Build the "native" Docker Image

```
docker build -t charybdis-native -f Dockerfile.native .
```

Start the application:

```
docker run --rm -p 8080:8080 charybdis-native:latest
```