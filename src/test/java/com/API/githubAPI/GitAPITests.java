package com.API.githubAPI;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class GitAPITests {
    @BeforeAll
    public static void setUp(){
        RestAssured.baseURI = "https://api.github.com";
    }

    @Test
    public void verifyOrganizationInformation(){
        /*
        1. Send a get request to /orgs/:org. Request includes : • Path param org with value cucumber
        2. Verify status code 200, content type application/json; charset=utf-8
        3. Verify value of the login field is cucumber
        4. Verify value of the name field is cucumber
        5. Verify value of the id field is 320565
         */

        //https://developer.github.com/v3/orgs/
        given().
                log().all().
                pathParam("org", "cucumber").
        when().
                get("/orgs/{org}").
        then().
                statusCode(200).
                contentType(ContentType.JSON).
                //contentType("application/json; charset=utf-8").
                body("login",is("cucumber")).
                body("name", is("Cucumber")).
                body("id",is(320565));
    }

    /*
    Verify error message
    1. Send a get request to /orgs/:org. Request includes :
        • Header Accept with value application/xml
        • Path param org with value cucumber
    2. Verify status code 415, content type application/json; charset=utf-8
    3. Verify response status line include message Unsupported Media Type
     */

    @Test
    public void verifyErrorMessage(){
        Response response = given().
                log().all().
                header("Accept", "application/xml").
                pathParam("org", "cucumber").
                when().
                get("/orgs/{org}").prettyPeek();

        response.then().
                statusCode(415).
                contentType(ContentType.JSON);

        assertThat(true, is(response.statusLine().contains("Unsupported Media Type")));
    }

    /*
    Number of repositories
    1. Send a get request to /orgs/:org. Request includes :
    • Path param org with value cucumber
    2. Grab the value of the field public_repos
    3. Send a get request to /orgs/:org/repos. Request includes :
    • Path param org with value cucumber
    4. Verify that number of objects in the response is equal to value from step 2
    By default /orgs/:org/repos only returns 30 repos per request.
    You may need to add query param per_page with any value that shows all the available repos for that user.
    Learn more about pagination in GitHub api at https://developer.github.com/v3/.
     */

    //https://developer.github.com/v3/repos/

    @Test
    public void NumberOfRepositories(){
        JsonPath jsonPath = given().
                log().all().
                pathParam("org", "cucumber").
                when().
                get("/orgs/{org}").jsonPath();

        int publicRepos = jsonPath.getInt("public_repos");

        jsonPath = given().log().all().
                pathParam("org","cucumber").
                queryParam("per_page",150).
                when().get("/orgs/{org}/repos").jsonPath();

        List<Object> repos = jsonPath.getList("id");
        assertThat(publicRepos,is(repos.size()));
    }


    /*
    Repository id information
    1. Send a get request to /orgs/:org/repos. Request includes : • Path param org with value cucumber
    2. Verify that id field is unique in every in every object in the response
    3. Verify that node_id field is unique in every in every object in the response
     */
    @Test
    public void repositoryIdInformation(){
        JsonPath jsonPath = given().log().all().
                pathParam("org","cucumber").
                queryParam("per_page", 100).
                when().get("/orgs/{org}/repos").jsonPath();
        List<String> listReposId = jsonPath.getList("id");
        Set<Object> setReposId = new HashSet<>(listReposId);
        assertThat(listReposId.size(),is(setReposId.size()));

        List<Object> listNodeId = jsonPath.getList("node_id");
        Set<Object> setNodeId = new HashSet<>(listNodeId);
        assertThat(listNodeId.size(),is(setNodeId.size()));
    }

    /*
    Repository owner information
    1. Send a get request to /orgs/:org. Request includes : • Path param org with value cucumber
    2. Grab the value of the field id
    3. Send a get request to /orgs/:org/repos. Request includes :
    • Path param org with value cucumber
    4. Verify that value of the id inside the owner object in every response is equal to value from step 2
     */

    @Test
    public void RepositoryOwnerInformation(){
        int id = given().pathParam("org", "cucumber").
                when().get("/orgs/{org}").jsonPath().getInt("id");

        given().pathParam("org", "cucumber").
                when().get("/orgs/{org}/repos").
        then().
                body("owner.id",everyItem(equalTo(id)));
    }


    /*
    Ascending order by full_name sort
    1. Send a get request to /orgs/:org/repos. Request includes :
    • Path param org with value cucumber
    • Query param "sort" with value full_name
    2. Verify that all repositories are listed in alphabetical order based on the
    value of the field name
     */

    @Test
    public void ascendingOrderByFullNameSort(){
        List<String> listFullName = given().
                pathParam("org", "cucumber").
                queryParam("sort", "full_name").
                when().get("/orgs/{org}/repos").jsonPath().getList("full_name");
        List<String> copyOfListFullName = new ArrayList<>(listFullName);
        Collections.sort(copyOfListFullName);
        assertThat(listFullName,is(copyOfListFullName));
    }

    /*
    Descending order by full_name sort
    1. Send a get request to /orgs/:org/repos. Request includes :
    • Path param org with value cucumber
    • Query param sort with value full_name
    • Query param direction with value desc
    2. Verify that all repositories are listed in reverser alphabetical order
    based on the value of the field name
     */

    @Test
    public void descendingOrderByFullNameSort(){
        List<String> listFullName = given().
                pathParam("org", "cucumber").
                queryParam("sort", "full_name").
                queryParam("direction","desc").
                when().get("/orgs/{org}/repos").jsonPath().getList("full_name");
        List<String> copyOfListFullName = new ArrayList<>(listFullName);
        Collections.sort(copyOfListFullName,Collections.reverseOrder());
        assertThat(listFullName,is(copyOfListFullName));
    }


    /*
    Default sort
    1. Send a get request to /orgs/:org/repos. Request includes :
    • Path param org with value cucumber
    2. Verify that by default all repositories are listed in descending order
    based on the value of the field created_at
     */

    @Test
    public void DefaultSort(){
        List<String> listCreatedDates = given().
                pathParam("org", "cucumber").
         when().
                get("/orgs/{org}/repos").jsonPath().getList("created_at");

        List<String> listDatesOnly = new ArrayList<>();

        for(String listCreatedDate : listCreatedDates){
            listDatesOnly.add(listCreatedDate.split("T")[0]);
        }
        List<String> copyOfListDatesOnly = new ArrayList<>(listDatesOnly);
        Collections.sort(copyOfListDatesOnly,Collections.reverseOrder());

        assertThat(listDatesOnly,is(copyOfListDatesOnly));

    }

}
