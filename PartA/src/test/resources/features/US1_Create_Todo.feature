Feature: Create Todo
  As a user,
  I want to create a todo
  So that I can track of my tasks

  Background:
    Given the thingifier application is running
    And no todo exists in the application except the following:
      | title           | doneStatus | description                |
      | ECSE429-PartA   | true       | First part of the project  |
      | ECSE429-PartC   | false      | Last part of the project   |

  Scenario Outline: User attempts to create a new Todo with Json body (Normal Flow)
    When the user sends a POST request to the "/todos" endpoint with JSON body with title "<title>" done status "<doneStatus>" and description "<description>"
    Then the thingifier app should return a response with status code "201"
    And the response body should be a JSON object with the following key-value pairs:
      | key          | value          |
      | title        | <title>        |
      | doneStatus   | <doneStatus>   |
      | description  | <description>  |

    Examples:
      | title          | doneStatus | description                |
      | ECSE429-PartB  | false      | Second part of the project |
      | ECSE429-PartD  | true       | Fourth part of the project |


  Scenario Outline: User attempts to create a new Todo with Xml body (Alternative Flow)
    When the user sends a POST request to the "/todos" endpoint with XML body with title "<title>" done status "<doneStatus>" and description "<description>"
    Then the thingifier app should return a response with status code "201"
    And the response body should be an XML object with the following elements:
      | element       | value          |
      | title         | <title>        |
      | doneStatus    | <doneStatus>   |
      | description   | <description>  |

    Examples:
      | title          | doneStatus | description                |
      | ECSE429-PartB  | false      | Second part of the project |
      | ECSE429-PartD  | true       | Fourth part of the project |


  Scenario Outline: User attempts to create a new Todo with no title (Error Flow)
    When the user sends a POST request to the "/todos" endpoint with JSON body with title "<title>" done status "<doneStatus>" and description "<description>"
    Then the thingifier app should return a response with status code "400"
    And the response body should be a JSON object with the following key-value pairs:
      | key             | value                    |
      | errorMessages   | <errorMessage>           |

    Examples:
      | title | doneStatus  | description         | errorMessage                                 |
      |       | true        | No title provided   | Failed Validation: title : can not be empty  |
      |       | false       | Empty title         | Failed Validation: title : can not be empty  |
      |       |             |                     | Failed Validation: title : can not be empty  |

