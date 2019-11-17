---
title: "Architecture"
date: 2019-11-17T20:54:34+03:00
draft: false
weight: 100
---

# Architecture concepts

![Architecture image](/graphs/architecture.png)

ConfigManager is abstract class that should be implemented by configuration
object (or a singleton class for Java) and provide declarative DSL for
configuration structure declaration just like
[Konfig library](https://github.com/npryce/konfig). This object stores a link
to channel, that takes stream of `RawProperties[]` filled by
`ConfigSource`s, and emmits changes to flow of properties.

Each registered property provides a Reloadable instance, that can be fetched by
get method of `ConfigManager`.
