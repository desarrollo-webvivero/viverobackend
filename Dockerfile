FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia el ejecutable compilado
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Copia la carpeta de la Wallet de Oracle
#COPY Wallet_BdPensamiento /app/Wallet_BdPensamiento

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]