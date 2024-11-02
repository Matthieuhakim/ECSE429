Feature: Delete Todo
    As a user,
    I want to delete a todo
    So that I can remove a non-relevant task from my list

    Background:
        Given the thingifier application is running
        And no todo exists in the application except the following:
            | title           | doneStatus | description                |
            | ECSE429-PartA   | true       | First part of the project  |
            | ECSE429-PartC   | false      | Last part of the project   |

    Scenario Outline: User attempts to delete an existing todo (Normal Flow)
        When the user sends a DELETE request to the "/todos/:id" endpoint with the id of the todo with title "<title>"
        Then the thingifier app should return a response with status code "200"
        And the thingifier app should not contain the todo with title "<title>"

        Examples:
            | title         |
            | ECSE429-PartA |
            | ECSE429-PartC |

    Scenario Outline: User attempts to delete an existing todo with Json parameters (Alternate Flow)
        When the user sends a DELETE request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with title
        Then the thingifier app should return a response with status code "200"
        And the thingifier app should not contain the todo with title "<title>"

        Examples:
            | title         |
            | ECSE429-PartA |
            | ECSE429-PartC |

    Scenario Outline: User attempts to delete a non-existing todo (Error Flow)
      When the user sends a DELETE request to the "/todos/:id" endpoint with the id of the todo with title "<title>"
      Then the thingifier app should return a response with status code "404"
      And the response body should be a JSON object with the following key-value pairs:
        | key             | value                    |
        | errorMessages   | <errorMessage>           |

      Examples:
        | title               | errorMessage                                |
        |   ECSE429-PartZ     | Could not find any instances with todos/0   |
        |   ECSE429-PartSA    | Could not find any instances with todos/0   |
        |   ECSE429-PartB     | Could not find any instances with todos/0   |
