# The builder image will not be used at runtime
# See https://docs.docker.com/develop/develop-images/multistage-build/
FROM gradle:6.8.2 as builder
#Copy sourcecode into build image
RUN mkdir ./app
COPY build.gradle ./app
COPY src ./app/
# We are building inside the app directory
RUN cd app && gradle clean build --no-daemon
#For the runtime image, use the latest Java, in a "slim" variant
FROM adoptopenjdk/openjdk15:debianslim-jre
# Put the one necessary file, with  all dependencies, into the   into the runtime,
COPY --from=builder ./home/gradle/app/build/libs/app.jar .
#Run it
CMD [ "java", "-jar",  "./app.jar" ]