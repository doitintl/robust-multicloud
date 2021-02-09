# The builder image will not be used at runtime
# See https://docs.docker.com/develop/develop-images/multistage-build/
FROM gradle:6.7.1 as builder

#Copy sourcecode into build image
COPY app/build.gradle ./app/
COPY app/src/ ./app/src/
COPY settings.gradle ./

RUN 1>&2 ls -R

RUN gradle clean build --no-daemon

# Now switch to the runtime image; base it on the latest Java, in a "slim" variant.
FROM adoptopenjdk/openjdk13:debianslim-jre
# Put the one necessary file, with  all dependencies, into the into the runtime.
COPY service-account.json .
COPY --from=builder ./home/gradle/app/build/libs/app.jar .
ENV GOOGLE_APPLICATION_CREDENTIALS service-account.json
# Run it
CMD [ "java", "-jar",  "./app.jar" ]