# gorgon
A Go playing program

To build

```
 ./gradlew clean jar
```

To run

```
java -jar build/libs/gorgon-1.0-SNAPSHOT.jar [engine params]
```

## History

### 2021-10-28
version 0.1.0. the best engine is called 'feature', which uses 3x3 and 5x5
pattern features, atari checks, and rough area influence to decide which square
is best to move to.  No search/lookahead is done.

This wins about 70% of the time against AmiGo (a spread of about 150 elo
points).  Looking at the ratings at
https://github.com/breakwa11/GoAIRatings/blob/master/README.md, I 
estimate the rating is about -25.

