FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew installDist

EXPOSE 8080

CMD ["./build/install/tictactoe/bin/tictactoe"]