---
title: "Functional specs"
description: "Functional testing with Gatling"
lead: "Write functional tests with Gatling, API test"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-04-20T18:30:56+02:00
weight: 2080700
---

Functional specs support was initially contributed by [Constantijn Visinescu](https://github.com/constantijn).

## Writing functional specs with gatling

In addition to writing load tests Gatling's DSL also allows you to write functional specs that are meant to run along
your integration or unit tests. This way you can write end to end tests using Gatling's DSL. This is especially useful
when testing REST APIs but it's designed to work with anything you can retrieve over http.

Functional tests written with Gatling look like this:

{{< include-code "GatlingFunSpecSample.scala#example-test" scala >}}

1. Have your test class extend GatlingHttpFunSpec to start writing a Gatling functional spec.
2. Set the base URL of the endpoint you want to test.
3. If needed you can overwrite or add to the gatling http configuration here by adding to or overriding the httpProtocol object. For a detailed description of all the additional options look [here]({{< ref "../../http/protocol" >}}).
4. Every spec gets wrapped in it's own spec block, you can have multiple specs per test.
5. Every test should start with a human readable description of what you're testing.
6. The URL (relative to the baseUrl) that you want to run checks on. (Post and other http methods are also supported).
7. Start running your checks. For more info on what kind of checks you can run look [here]({{< ref "../../http/check" >}})
8. As a best practice you should put all your check type definitions in a companion object and your validations on them in the check statements. This leads to much more readable tests and less code duplication.

## Quick start

We have an [demo project](https://github.com/gatling/gatling-funspec-demo/) configured to run functional specs with sbt to help you get started right away.
