Feature: Mark Todo Done
  As a user,
  I want to mark a todo as done
  So that I know that the task is completed


  Background:
    Given the thingifier application is running
    And no todo exists in the application except the following:
      | title           | doneStatus | description                |
      | ECSE429-PartA   | true       | First part of the project  |
      | ECSE429-PartC   | false      | Last part of the project   |

  Scenario Outline: User attempts to mark an existing todo as done with Json body (Normal Flow)
    When the user sends a POST request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with done status "<doneStatus>"
    Then the thingifier app should return a response with status code "200"
    And the response body should be a JSON object with the following key-value pairs:
      | key        | value   |
      | title      | <title> |
      | doneStatus | true    |

    Examples:
      | title         | doneStatus |
      | ECSE429-PartC | true       |
      | ECSE429-PartA | true       |

  Scenario Outline: User attempts to mark an existing todo as done with XML body (Alternate Flow)
    When the user sends a POST request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and XML body with done status "<doneStatus>"
    Then the thingifier app should return a response with status code "200"
    And the response body should be an XML object with the following elements:
      | element        | value   |
      | title          | <title> |
      | doneStatus     | true    |

    Examples:
      | title         | doneStatus |
      | ECSE429-PartC | true       |
      | ECSE429-PartA | true       |

  Scenario Outline: User attempts to mark an existing todo as done with invalid done status (Error Flow)
    When the user sends a POST request to the "/todos/:id" endpoint with the id of the todo with title "<title>" and JSON body with done status "<doneStatus>"
    Then the thingifier app should return a response with status code "400"
    And the response body should be a JSON object with the following key-value pairs:
      | key           | value                                             |
      | errorMessages | Failed Validation: doneStatus should be BOOLEAN   |

    Examples:
        | title         | doneStatus  |
        | ECSE429-PartA | sauygewv    |
        | ECSE429-PartC |             |
        | ECSE429-PartC | done        |

