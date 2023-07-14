package com.benrhine.lambda.gradletemplate.v1.lambda.aws.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.benrhine.lambda.gradletemplate.v1.model.Person;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * --------------------------------------------------------------------------------------------------------------------
 * PersonHandler: TODO fill me in.
 * ------------------------------------------------------------------------------------------------------------------
 */
public class PersonHandler implements RequestStreamHandler {

    private static  Logger logger = Logger.getLogger(PersonHandler.class.getName());

    private static final String DYNAMODB_TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDb = new DynamoDB(client);

        try {
            JSONObject event = (JSONObject) parser.parse(reader);

            logger.info("here 1");

            if (event.get("body") != null) {
                logger.info("here 2");
                Person person = new Person((String) event.get("body"));

                logger.info("here 3: " + person.getName());

                dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                        .putItem(new PutItemSpec().withItem(new Item().withNumber("id", person.getId())
                                .withString("name", person.getName())));
                logger.info("here 4");
            } else {
                logger.info(event.toString());
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "New item created");

            JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-header", "my custom header value");

            responseJson.put("statusCode", 200);
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        }
        logger.info("here 5");

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    public void handleGetByParam(
            InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {

        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDb = new DynamoDB(client);

        Item result = null;
        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            JSONObject responseBody = new JSONObject();
            logger.info("here 1");

            if (event.get("pathParameters") != null) {
                logger.info("here 2");
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("id") != null) {
                    logger.info("here 2.5");
                    logger.info((String) pps.get("id"));
                    int id = Integer.parseInt((String) pps.get("id"));
                    logger.info("id: " + id);
                    result = dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("id", id);
                }
            } else if (event.get("queryStringParameters") != null) {
                logger.info("here 3");
                JSONObject qps = (JSONObject) event.get("queryStringParameters");
                if (qps.get("id") != null) {
                    logger.info("here 3.5");
                    int id = Integer.parseInt((String) qps.get("id"));
                    result = dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                            .getItem("id", id);
                }
            }
            if (result != null) {
                logger.info("here 4");
                Person person = new Person(result.toJSON());
                responseBody.put("Person", person);
                responseJson.put("statusCode", 200);
            } else {
                logger.info("here 5");
                responseBody.put("message", "No item found");
                responseJson.put("statusCode", 404);
            }

            JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-header", "my custom header value");

            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }
}
