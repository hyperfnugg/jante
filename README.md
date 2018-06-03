# Jante
#####Problem:
General purpose frameworks are optimized for certain conventions. Sometimes the needs of a team diverge significantly
from said conventions.
In these cases, using a general purpose framework requires writing custom configuration and code.
Such customization requires familiarity
with the framework, and often must compromises between solving the problem and accomodating the framework.

#####Proposition
Consider having each team write and maintain their own microservice chassis. There are several advantages:
* The common code implements exactly the behavior you need and nothing more - which means the minimum
behavior to understand and maintain
* The common code can assume defaults tailored to you, thus reducing the interface of the common code. With a minimum
interface, you might be able to update the common code without disturbing the business logic
* You can easily adapt to common behavior not covered by general purpose frameworks.
* Writing a microservice chassis is often about configuring and integrating many libraries. If you choose
the right libraries, the configuration and intergration does not require a lot of code.
* Having highly standardized applications makes it easier to integrate applications



##The code
The attached library is an example of a microservice chassis written from scratch. You are free to fork it as a starting
point for your own code.

To install with archetype (Does not seem to work to well with ideas integrated maven):
```
mvn clean install -Darchetype
```
