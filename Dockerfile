# The builder image will not be used at runtime
# See https://docs.docker.com/develop/develop-images/multistage-build/
FROM gradle:6.7.1 as builder

#Copy sourcecode into build image
COPY app/build.gradle ./app/
COPY app/src/ ./app/src/
COPY settings.gradle ./
RUN gradle clean build --no-daemon

RUN java -jar ./home/gradle/app/build/libs/app.jar
# Now switch to the runtime image; base it on the latest Java, in a "slim" variant.
FROM adoptopenjdk/openjdk13:debianslim-jre
# Put the one necessary file, with  all dependencies, into the into the runtime.
COPY --from=builder ./home/gradle/app/build/libs/app.jar .
# Run it
CMD [ "java", "-jar",  "./app.jar" ]