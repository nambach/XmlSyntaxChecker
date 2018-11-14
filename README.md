# XML Syntax Checker
An engine to check syntax of HTML files and convert them into XML format, serve along with an XML crawler.

## I. Syntax Checker
The syntax checker includes 2 modules: 
1. `XmlSyntaxChecker` refines attributes inside opening tags, and makes the document well-formed by adding missing opening/closing tags.
2. `EntitySyntaxChecker` finds all entities that are not defined for XML and replaces all ampersand character `&` with ampersand notation `&amp;`

## Philosophy
The **Syntax Checker** engine works as a *language resolver*.
Instead of using flags to mark the language's syntax, the resolver uses *state-machine* mechanism:
whenever it hits a particular character, the engine changes into the corresponding state.
Therefore, there is only one `state` variable to manage (instead of bunch of flags), and the game is much easier.

### XML State Diagram
![XML Syntax Diagram](https://github.com/nambach/XmlSyntaxChecker/blob/master/XMLSyntax.svg)

### Entity State Diagram
![Entity Syntax Diagram](https://github.com/nambach/XmlSyntaxChecker/blob/master/EntitySyntax.svg)

## II. Crawler
### Mechanism
When start crawling, the `Crawler` sequentially does these steps:
1. Read the rules provided in XML file (![Schema](https://github.com/nambach/XmlSyntaxChecker/blob/master/src/main/java/crawler/rule/crawling.xsd)) or `Rules` objects.
2. Fetch HTML content as text (based on crawling rules) and convert them into XML format (using `XmlSyntaxChecker` and `EntitySyntaxChecker`)
3. Parse document into DOM tree using DOM parser and start extracting data based on extraction rules.
### Processing crawled data
The extracted data present as a list of `Map<String, String>`. 
When initializing `Crawler`, we need to pass a parameter class that implements `CrawlerResultProcessor` to process the result list after finish crawling data.
```
CrawlerResultProcessor processor = new BookProcessor();
Crawler<BookProcessor> crawler = new Crawler<>();
crawler.setResultProcessor(processor);
...
crawler.crawl();
```
The `CrawlerResultProcessor` has 3 degrees of processing crawled data:
- Object: anytime after successfully extracting data as a discrete object; provided in `processResultObject(Map<String, String> object)`
- Fragment: anytime after finish extracting all objects in one HTML page (usually range from 20-50 objects); provided in `processResultFragmentList(List<Map<String, String>> list)`
- List: only after the whole process finishes, it returns a list of all objects as a final result, provided in `processResultList(List<Map<String, String>> list)`
### Stop the crawler
`Crawler` uses a static variable *`STOP`* to control the work flow. To immediately stop the process, simply set it false
```
Crawler.STOP = false;
```
Once the crawler has stopped, you must set it back to true before starting any other crawling progresses.
