#!/bin/bash

docker run -d -p 8000:8000 amazon/dynamodb-local -jar DynamoDBLocal.jar -inMemory -sharedDb

aws dynamodb create-table --cli-input-json file://table_schema.json  --endpoint-url "http://localhost:8000"
