---
title: AMQP 0.9.1 protocol binding
keywords: binding, protocol, amqp, amqp091, rabbitmq
tags: [protocol, connectivity, rql]
permalink: connectivity-protocol-bindings-amqp091.html
---

When messages are sent in [Ditto Protocol](protocol-overview.html) (as `UTF-8` encoded String payload), 
the `content-type` of AMQP 0.9.1 messages must be set to:

```
application/vnd.eclipse.ditto+json
```

If messages which are not in Ditto Protocol should be processed, a [payload mapping](connectivity-mapping.html) must
be configured for the AMQP 0.9.1 connection in order to transform the messages. 

## AMQP 0.9.1 properties

Supported AMQP 0.9.1 properties which are interpreted in a specific way are:

* `content-type`: for defining the Ditto Protocol content-type
* `correlation-id`: for correlating request messages to responses

## Specific connection configuration

### Source format

An AMQP 0.9.1 connection requires the protocol configuration source object to have an `addresses` property with a list
of queue names and `authorizationContext` array that contains the authorization subjects in whose context 
incoming messages are processed. These subjects may contain placeholders, see 
[placeholders](basic-connections.html#placeholder-for-source-authorization-subjects) section for more information.


```json
{
  "addresses": [
    "<queue_name>",
    "..."
  ],
  "authorizationContext": ["ditto:inbound-auth-subject", "..."]
}
```

### Target format

An AMQP 0.9.1 connection requires the protocol configuration target object to have an `address` property with a combined
value of the `exchange_name` and `routing_key`. The target address may contain placeholders; see
[placeholders](basic-connections.html#placeholder-for-target-addresses) section for more information.

Further, `"topics"` is a list of strings, each list entry representing a subscription of
[Ditto protocol topics](protocol-specification-topic.html).

Outbound messages are published to the configured target address if one of the subjects in `"authorizationContext"`
have READ permission on the Thing that is associated with a message.


```json
{
  "address": "<exchange_name>/<routing_key>",
  "topics": [
    "_/_/things/twin/events",
    "_/_/things/live/messages"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."]
}
```

#### Filtering 

In order to only consume specific events like described in [change notifications](basic-changenotifications.html), the
following parameters can additionally be provided when specifying the `topics` of a target:

| Description | Topic | Filter by namespaces | Filter by RQL expression |
|-------------|-----------------|------------------|-----------|
| Subscribe for [events/change notifications](basic-changenotifications.html) | `_/_/things/twin/events` | yes | yes |
| Subscribe for [messages](basic-messages.html) | `_/_/things/live/messages` | yes | |
| Subscribe for [live commands](protocol-twinlive.html) | `_/_/things/live/commands` | yes |  |
| Subscribe for [live events](protocol-twinlive.html) | `_/_/things/live/events` | yes | yes |

The parameters are specified similar to HTTP query parameters, the first one separated with a `?` and all following ones
with `&`. You have to URL encode the filter values before using them in a configuration.

For example this way the connection session would register for all events in the namespace `org.eclipse.ditto` and which
would match an attribute "counter" to be greater than 42. Additionally it would subscribe to messages in the namespace
`org.eclipse.ditto`:
```json
{
  "address": "<exchange_name>/<routing_key>",
  "topics": [
    "_/_/things/twin/events?namespaces=org.eclipse.ditto&filter=gt(attributes/counter,42)",
    "_/_/things/live/messages?namespaces=org.eclipse.ditto"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."]
}
```


### Specific configuration properties

There are no specific configuration properties available for this type of connection.

## Establishing connecting to an AMQP 0.9.1 endpoint

Ditto's [Connectivity service](architecture-services-connectivity.html) is responsible for creating new and managing 
existing connections.

This can be done dynamically at runtime without the need to restart any microservice using a
[Ditto DevOps command](installation-operating.html#devops-commands).

Example connection configuration to create a new AMQP 0.9.1 connection (e.g. in order to connect to a RabbitMQ):

```json
{
  "connection": {
    "id": "rabbit-example-connection-123",
    "connectionType": "amqp-091",
    "connectionStatus": "open",
    "failoverEnabled": true,
    "uri": "amqp://user:password@localhost:5672/vhost",
    "sources": [
      {
        "addresses": [
          "queueName"
        ],
        "authorizationContext": ["ditto:inbound-auth-subject", "..."]
      }
    ],
    "targets": [
      {
        "address": "exchangeName/routingKey",
        "topics": [
          "_/_/things/twin/events",
          "_/_/things/live/messages"
        ],
        "authorizationContext": ["ditto:outbound-auth-subject", "..."]
      }
    ]
  }
}
```

## Messages

Messages consumed via the AMQP 0.9.1 binding are treated similar to the [WebSocket binding](httpapi-protocol-bindings-websocket.html)
meaning that the messages are expected to be [Ditto Protocol](protocol-overview.html) messages serialized as JSON (as 
shown for example in the [protocol examples](protocol-examples.html)). If your payload is not conform to the [Ditto
Protocol](protocol-overview.html), you can configure a custom [payload mapping](connectivity-mapping.html).
 
