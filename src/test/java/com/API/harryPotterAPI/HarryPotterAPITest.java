package com.API.harryPotterAPI;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.API.harryPotterAPI.pojos.House;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

public class HarryPotterAPITest {
    @BeforeAll
    public static void setUp(){
        baseURI = "https://www.potterapi.com/v1";
    }

    /*
    Verify sorting hat
    1. Send a get request to /sortingHat. Request includes :
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Verify that response body contains one of the following houses:
    "Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"
     */

    @Test
    public void VerifySortingHat(){
        Response response = given().log().all().
                when().get("/sortingHat").prettyPeek();

        response.then().statusCode(200).
                    contentType(ContentType.JSON);

        String name = new House(response.asString().replace("\"","")).getName();
        List<String> listOfHouses = new ArrayList<>(Arrays.asList("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"));
        assertThat(true,is(listOfHouses.contains(name)));
    }

    /*
    Verify bad key
    1. Send a get request to /characters. Request includes :
    • Header Accept with value application/json
    • Query param key with value invalid
    2. Verify status code 401, content type application/json; charset=utf-8
    3. Verify response status line include message Unauthorized
    4. Verify that response body says "error": "API Key Not Found"
     */

    @Test
    public void verifyBadKey(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "invalid").
                when().get("/characters").prettyPeek();

        response.then().contentType(ContentType.JSON).
                statusCode(401).
                body("error",is("API Key Not Found"));
        assertThat((response.statusLine().contains("Unauthorized")),is(true));
        assertThat(response.asString().contains("\"error\":\"API Key Not Found\""), is(true));

    }


    /*
    Verify no key
    1. Send a get request to /characters. Request includes :
    • Header Accept with value application/json
    2. Verify status code 409, content type application/json; charset=utf-8
    3. Verify response status line include message Conflict
    4. Verify that response body says "error": "Must pass API key for request"
     */

    @Test
    public void verifyNoKey(){
        Response response = given().log().all().
                header("Accept", "application/json").
                when().
                get("/characters");
        response.then().statusCode(409).contentType(ContentType.JSON).
        body("error",is("Must pass API key for request"));
        assertThat(response.statusLine().contains("Conflict"),is(true));
       // assertThat(response.asString().contains("\"error\":\"Must pass API key for request\""),is(true));
    }

    /*
    Verify number of characters
    1. Send a get request to /characters. Request includes :
    • Header Accept with value application/json
    • Query param key with value {{apiKey}}
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Verify response contains 194 characters
     */

    @Test
    public void verifyNumberOfCharacters(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key","$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
        when().
                get("/characters");

        response.then().statusCode(200).contentType(ContentType.JSON);
        List<Object> items = response.jsonPath().getList("_id");
        assertThat(items.size(),is(194));
    }

    /*
    Verify number of character id and house
    1. Send a get request to /characters. Request includes : • Header Accept with value application/json
    • Query param key with value {{apiKey}}
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Verify all characters in the response have id field which is not empty
    4. Verify that value type of the field dumbledoresArmy is a boolean in all characters in the response
    5. Verify value of the house in all characters in the response is one of the following:
    "Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"
     */

    @Test
    public void verifyNumberOfCharacterIdAndHouse(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key","$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
        when().
                get("/characters");
        boolean check = false;
        List<Boolean> dumbledoresArmy = response.jsonPath().getList("dumbledoresArmy");
        for (Boolean each: dumbledoresArmy){
            if (each == true || each == false){
                check = true;
            }
        }
        response.then().statusCode(200).contentType(ContentType.JSON).
                body("_id",everyItem(not(empty())));
        assertThat(true, is(check));

        List<String> houses = response.jsonPath().getList("findAll{it.house}.house");
        System.out.println(houses);

        List<String> listOfHouses = new ArrayList<>(Arrays.asList("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"));
        check = true;
        for (String listOfHouse : houses) {
            if (!listOfHouses.contains(listOfHouse)){
                check = false;
                break;
            }
        }
        assertThat(true, is(check));
        //        assertThat(houses,everyItem(isIn(listOfHouses))); this one is

    }

    /*
    Verify all character information
    1. Send a get request to /characters. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Select name of any random character
    4. Send a get request to /characters. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Query param name with value from step 3
    5. Verify that response contains the same character information from step 3. Compare all fields.
     */

    @Test
    public void verifyAllCharacterInformation(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key","$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
        when().
                get("/characters");
        response.then().statusCode(200).contentType(ContentType.JSON);
        List<Map<String, Object>> characters = response.jsonPath().getList("");

        Random rd = new Random();
        int randomNumber = rd.nextInt(characters.size());
        String randomName = characters.get(randomNumber).get("name").toString();

        List<Map<String, Object>> actualCharacters = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                queryParam("name", randomName).
                when().get("/characters").jsonPath().getList("");
        assertThat(actualCharacters.get(0),is(characters.get(randomNumber)));
    }

    /*
    Verify name search
    1. Send a get request to /characters. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Query param name with value Harry Potter
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Verify name Harry Potter
    4. Send a get request to /characters. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Query param name with value Marry Potter
    5. Verify status code 200, content type application/json; charset=utf-8
    6. Verify response body is empty
     */

    @Test
    public void verifyNameSearch(){
       given().log().all().
                header("Accept", "application/json").
                queryParam("key","$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                queryParam("name","Harry Potter").
       when().
                get("/characters").
       then().
               statusCode(200).
               contentType(ContentType.JSON).
               body("[0].name", is("Harry Potter"));

        given().log().all().
                header("Accept", "application/json").
                queryParam("key","$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                queryParam("name","Marry Potter").
        when().
                get("/characters").
        then().statusCode(200).
                contentType(ContentType.JSON).
                body("", is(empty()));

    }

    /*
    Verify house members
    1. Send a get request to /houses. Request includes :
         • Header Accept with value application/json
         • Query param key with value {{apiKey}}
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Capture the id of the Gryffindor house
    4. Capture the ids of the all members of the Gryffindor house
    5. Send a get request to /houses/:id. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Path param id with value from step 3
    6. Verify that response contains the same member ids as the step 4
     */

    @Test
    public void verifyHouseMembers(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
        when().
                get("/houses").prettyPeek();
        response.then().
                statusCode(200).
                contentType(ContentType.JSON);
        String gryffindorId = response.jsonPath().getString("find{it.name=='Gryffindor'}._id");

        List<Object> memberIds = response.jsonPath().getList("findAll{it.name=='Gryffindor'}.members[0]");

        response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                pathParam("id",gryffindorId).
        when().
                get("/houses/{id}").prettyPeek();
        List<Object> actualMembersId = response.jsonPath().getList("[0].members._id");

        assertThat(actualMembersId,is(memberIds));
    }

    /*
    Verify house members again
    1. Send a get request to /houses/:id. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Path param id with value 5a05e2b252f721a3cf2ea33f
    2. Capture the ids of all members
    3. Send a get request to /characters. Request includes :
        • Header Accept with value application/json
        • Query param key with value {{apiKey}}
        • Query param house with value Gryffindor
    4. Verify that response contains the same member ids from step 2
     */

    @Test
    public void verifyHouseMembersAgain(){
       Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                pathParam("id","5a05e2b252f721a3cf2ea33f").
       when().
                get("/houses/{id}");

        List<Object> expectedMembersId = response.jsonPath().getList("[0].members._id");


        response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
                queryParam("house","Gryffindor").
        when().
                get("/characters").prettyPeek();
        List<Object> actualMembersId = response.jsonPath().getList("_id");
        assertThat(actualMembersId,is(expectedMembersId));

    }


    /*
    Verify house with most members
    1. Send a get request to /houses. Request includes :
         • Header Accept with value application/json
         • Query param key with value {{apiKey}}
    2. Verify status code 200, content type application/json; charset=utf-8
    3. Verify that Gryffindor house has the most members
     */

    @Test
    public void verifyHouseWithMostMembers(){
        Response response = given().log().all().
                header("Accept", "application/json").
                queryParam("key", "$2a$10$s8DSvL7pIZx4pTGdYYw8GeG/W/U9AbBCUvK1VJ/d2SX1hr0eM6Rnq").
        when().
                get("/houses").prettyPeek();
        response.then().statusCode(200).contentType(ContentType.JSON);
        int gryffindorSize = response.jsonPath().getList("findAll{it.house='Gryffindor'}.members[0]").size();
        int ravenclawSize = response.jsonPath().getList("findAll{it.house='Ravenclaw'}.members[0]").size();
        int slytherinSize = response.jsonPath().getList("findAll{it.house='Slytherin'}.members[0]").size();
        int hufflepuffSize = response.jsonPath().getList("findAll{it.house='Hufflepuff'}.members[0]").size();
        List<Integer> houseSizes = new ArrayList<>(Arrays.asList(ravenclawSize,slytherinSize,hufflepuffSize));
        assertThat(houseSizes,everyItem(is(lessThanOrEqualTo(gryffindorSize))));
    }

}
