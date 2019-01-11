# BC-WSPro-Work-Pattern-Analyzer

This project has been developed by Can Bilgiç and Uğur Deniz Yavaş with bootcamp innovation week.

### Getting Started

These instructions will get you a copy of the project up and running on your local machine 
for development and testing purposes.

### Configuration
Some of properties must be set before using this application.
##### application name
```
application.name = WsPro
```
You can use default application name.

#### Google API Authorization
The first time you run or debug  the application, it will prompt you to authorize access:
```
Please open the following address in your browser:
https://accounts.google.com/o/oauth2/auth?client_id.........
```
if your browser will not open automatically, copy and paste link to browser and authorize.

## Running and Debuging
There are two ways of usage WsPro. 
1) Use with Jar. 
2) Open with IDE and run it with parameters.
### Creating Jar: 
- Open your project path with console.
- Run below command:
```
 * gradle bootJar
 As a result of this command, you will get a jar file like com.crossover-<version>.jar in /build/libs folder.
```

##### Usage of Jar
```
java -jar com.crossover-<version>.jar --startDate="2019-01-02" --endDate="2019-01-03" --mode="append" --credential="\path\credentials.properties"
```
* above command reports with given start date and end date, pull statistics of candidates on crossover team and save to sheet.

```
* startDate = "<yyyy-MM-dd>" and endDate = "<yyyy-MM-dd>"
* --credential="path to properties" : Your credential info to join and authenticate to app.crossover.com
```

###### Content of Credential.properties:
```
cross.username=user
cross.password=password
team.room.id=1****
sheet.id=13IZShJa96dp_y2TGyntJCbPhAW_*******
```

Note that, sheet id refers to google sheet id (docs.google.com/spreadsheets/d/id...).

```
* java -jar com.crossover-<version>.jar --startDate="2019-01-02" --endDate="2019-01-03" --mode="delete" --credential="\path\credentials.properties"
```

above command removes records from sheet between given start date and end date.

##### Usage from IDE:

```
* Import project from Intellij-Idea
* Run -> Edit Configuration.
* Add new configuration -> Groovy ->  
	* Script Path: \src\main\groovy\com\crossover\wspro\WSProRunner.groovy
	* Program Arguments: --startDate="2019-01-02" --endDate="2019-01-03" --mode="append" --credential="path\credentials.properties"
	* Apply and create. Later, run project with this configuration.
```
		
## Authors

* **Can Bilgiç** -  [github.com/bilgiccan](https://github.com/bilgiccan)
