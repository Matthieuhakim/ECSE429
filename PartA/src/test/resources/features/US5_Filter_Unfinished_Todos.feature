Feature: Filter Unfinished Todos
    As a user,
    I want to filter unfinished todos
    So that I can see only the tasks that are not completed

    Background:
        Given the thingifier application is running
        And no todo exists in the application except the following:
            | title           | doneStatus | description                |
            | ECSE429-PartA   | true       | First part of the project  |
            | ECSE429-PartC   | false      | Third part of the project  |
            | ECSE429-PartB   | false      | Second part of the project |
            | ECSE429-PartD   | true       | Last part of the project   |

    Scenario Outline: User attempts to filter all unfinished todos (Normal Flow)
        When the user sends a GET request to the "/todos?doneStatus=<doneStatus>" endpoint
        Then the thingifier app should return a response with status code "200"
        And the response body should be a list of JSON objects with the following key-value pairs:
            | title         | doneStatus | description                |
            | ECSE429-PartC | false      | Third part of the project  |
            | ECSE429-PartB | false      | Second part of the project |
        Examples:
            | doneStatus |
            | false      |

    Scenario Outline: User attempts to filter all finished todos (Alternative Flow)
        When the user sends a GET request to the "/todos?doneStatus=<doneStatus>" endpoint
        Then the thingifier app should return a response with status code "200"
        And the response body should be a list of JSON objects with the following key-value pairs:
            | title         | doneStatus | description               |
            | ECSE429-PartA | true       | First part of the project |
            | ECSE429-PartD | true       | Last part of the project  |
        Examples:
            | doneStatus |
            | true       |

    Scenario Outline: User attempts to filter all todos with invalid doneStatus (Error Flow)
        When the user sends a GET request to the "/todos?doneStatus=<doneStatus>" endpoint
        Then the thingifier app should return a response with status code "200"
        And the response body should be a JSON object with the following key-value pairs:
          | title         | doneStatus | description  |

        Examples:
            | doneStatus |
            | 1          |
            | 0          |
            | invalid    |
            | asighw     |


