# E-Commerce Recommendation Template

## Documentation

Please refer to http://docs.prediction.io/templates/ecommercerecommendation/quickstart/

## Versions

### v0.4.0

- Change from ALSAlgorithm.scala to ECommAlgorithm.scala

  * return popular bought items when no information is found for the user.
  * add "similarEvents" parameter for configuration what user-to-item events are used for finding similar items
  * re-structure the Algorithm code for easier customization and testing

- add some unit tests for testing code that may be customized

### v0.3.1

- use INVALID_APP_NAME as default appName in engine.json

### v0.3.0

- update for PredictionIO 0.9.2, including:

  - use new PEventStore and LEventStore API
  - use appName in DataSource and Algorithm parameters


### v0.2.0

- update build.sbt and template.json for PredictionIO 0.9.2

### v0.1.1

- update for PredictionIO 0.9.0

### v0.1.0

- initial version


## Development Notes

### import sample data

```
$ python data/import_eventserver.py --access_key <your_access_key>
```

### query

normal:

```
$ curl -H "Content-Type: application/json" \
-d '{
  "user" : "u1",
  "num" : 10 }' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
$ curl -H "Content-Type: application/json" \
-d '{
  "user" : "u1",
  "num": 10,
  "categories" : ["c4", "c3"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "user" : "u1",
  "num": 10,
  "whiteList": ["i21", "i26", "i40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "user" : "u1",
  "num": 10,
  "blackList": ["i21", "i26", "i40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

unknown user:

```
curl -H "Content-Type: application/json" \
-d '{
  "user" : "unk1",
  "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

### handle new user

new user:

```
curl -H "Content-Type: application/json" \
-d '{
  "user" : "x1",
  "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

import some view events and try to get recommendation for x1 again.

```
accessKey=<YOUR_ACCESS_KEY>
```

```
curl -i -X POST http://localhost:7070/events.json?accessKey=$accessKey \
-H "Content-Type: application/json" \
-d '{
  "event" : "view",
  "entityType" : "user"
  "entityId" : "x1",
  "targetEntityType" : "item",
  "targetEntityId" : "i2",
  "eventTime" : "2015-02-17T02:11:21.934Z"
}'

curl -i -X POST http://localhost:7070/events.json?accessKey=$accessKey \
-H "Content-Type: application/json" \
-d '{
  "event" : "view",
  "entityType" : "user"
  "entityId" : "x1",
  "targetEntityType" : "item",
  "targetEntityId" : "i3",
  "eventTime" : "2015-02-17T02:12:21.934Z"
}'

```

## handle unavailable items

Set the following items as unavailable (need to specify complete list each time when this list is changed):

```
curl -i -X POST http://localhost:7070/events.json?accessKey=$accessKey \
-H "Content-Type: application/json" \
-d '{
  "event" : "$set",
  "entityType" : "constraint"
  "entityId" : "unavailableItems",
  "properties" : {
    "items": ["i43", "i20", "i37", "i3", "i4", "i5"],
  }
  "eventTime" : "2015-02-17T02:11:21.934Z"
}'
```

Set empty list when no more items unavailable:

```
curl -i -X POST http://localhost:7070/events.json?accessKey=$accessKey \
-H "Content-Type: application/json" \
-d '{
  "event" : "$set",
  "entityType" : "constraint"
  "entityId" : "unavailableItems",
  "properties" : {
    "items": [],
  }
  "eventTime" : "2015-02-18T02:11:21.934Z"
}'
```
