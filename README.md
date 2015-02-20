# E-Commerce Recommendation Template

## Documentation

Please refer to http://docs.prediction.io/templates/ecommercerecommendation/quickstart/

## Versions

### develop


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


{"itemScores":[{"item":"i43","score":0.9987465791873191},{"item":"i21","score":0.9987465791873191},{"item":"i20","score":0.997849496769407},{"item":"i49","score":0.9969960024975738},{"item":"i41","score":0.9955226345451645},{"item":"i37","score":0.9953403563456491},{"item":"i8","score":0.9944547925923641},{"item":"i46","score":0.9928301245473273},{"item":"i1","score":0.9910679754780729},{"item":"i18","score":0.9903295848514115}]}
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
curl -i -X POST http://localhost:7070/events.json?accessKey=s49uNadpNh5C4cRGCCIp8NqK1UWkaQPsN1SN2y670pZfRVXsbUepzmZQmyfjWeHo \
-H "Content-Type: application/json" \
-d '{
  "event" : "view",
  "entityType" : "user"
  "entityId" : "x1",
  "targetEntityType" : "item",
  "targetEntityId" : "i2",
  "eventTime" : "2015-02-17T02:11:21.934Z"
}'

curl -i -X POST http://localhost:7070/events.json?accessKey=s49uNadpNh5C4cRGCCIp8NqK1UWkaQPsN1SN2y670pZfRVXsbUepzmZQmyfjWeHo \
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

Set the following items "unavailable" (need to specify complete list each time when this list is changed):

```
curl -i -X POST http://localhost:7070/events.json?accessKey=s49uNadpNh5C4cRGCCIp8NqK1UWkaQPsN1SN2y670pZfRVXsbUepzmZQmyfjWeHo \
-H "Content-Type: application/json" \
-d '{
  "event" : "$set",
  "entityType" : "itemConstraint"
  "entityId" : "unavailable",
  "properties" : {
    "items": ["i43", "i20", "i37", "i3", "i4", "i5"],
  }
  "eventTime" : "2015-02-17T02:11:21.934Z"
}'
```

No more items "unavailable":

```
curl -i -X POST http://localhost:7070/events.json?accessKey=s49uNadpNh5C4cRGCCIp8NqK1UWkaQPsN1SN2y670pZfRVXsbUepzmZQmyfjWeHo \
-H "Content-Type: application/json" \
-d '{
  "event" : "$set",
  "entityType" : "itemConstraint"
  "entityId" : "unavailable",
  "properties" : {
    "items": [],
  }
  "eventTime" : "2015-02-18T02:11:21.934Z"
}'
```
