Feature: Update Todo Attributes
  As a user,
  I want to update a todo's attributes
  So that the todo represents the updated information about the task

  Background:
    Given the thingifier application is running
    And no todo exists in the application except the following:
      | title           | doneStatus | description                |
      | ECSE429-PartA   | true       | First part of the project  |
      | ECSE429-PartC   | false      | Last part of the project   |


    Scenario Outline: User attempts to update the title of a todo with Json body (Normal Flow)
      When the user sends a PUT request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with title "<newTitle>"
      Then the thingifier app should return a response with status code "200"
      And the response body should be a JSON object with the following key-value pairs:
        | key    | value      |
        | title  | <newTitle> |

      Examples:
        | title         | newTitle          |
        | ECSE429-PartA | ECSE429-PartB     |
        | ECSE429-PartC | ECSE429-PartD     |

    Scenario Outline: User attempts to update the description of a todo with Json body (Alternate Flow)
        When the user sends a PUT request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with description "<newDescription>"
        Then the thingifier app should return a response with status code "200"
        And the response body should be a JSON object with the following key-value pairs:
            | key          | value            |
            | title        | <title>          |
            | description  | <newDescription> |

        Examples:
            | title         | newDescription                 |
            | ECSE429-PartA | First part of the project 2    |
            | ECSE429-PartC | Last part of the project 2     |

    Scenario Outline: User attempts to update the title of a todo with empty title (Error Flow)
        When the user sends a PUT request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with title "<newTitle>"
        Then the thingifier app should return a response with status code "400"
        And the response body should be a JSON object with the following key-value pairs:
            | key             | value                    |
            | errorMessages   | <errorMessage>           |

        Examples:
            | title              | newTitle  | errorMessage                                 |
            |   ECSE429-PartA    |           | Failed Validation: title : can not be empty  |
            |   ECSE429-PartA    |           | Failed Validation: title : can not be empty  |
            |   ECSE429-PartC    |           | Failed Validation: title : can not be empty  |