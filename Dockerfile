FROM openjdk:17-alpine

RUN mkdir "app"
WORKDIR /app

COPY ./build/libs/order-management.jar .

ENV SERVER_PORT=8282
ENV KAFKA_BOOTSTRAP_SERVERS='localhost:9092,localhost:9093'

ENV KAFKA_ORDER_TOPIC_NAME='orders.topic'
ENV KAFKA_ORDER_DLT_TOPIC_NAME='orders.topic.dlt'
ENV KAFKA_SUCCESSFUL_ORDER_TOPIC_NAME='orders.success.topic'
ENV KAFKA_FAILED_ORDER_TOPIC_NAME='orders.failed.topic'

ENV POSTGRES_URL='jdbc:postgresql://localhost:5432/order_management?currentSchema=gvggroup'
ENV POSTGRES_USER='postgres'
ENV POSTGRES_PASSWORD=''

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-jar"]

CMD ["order-management.jar"]