JSourceMap
==========

JSourceMap is a library to parse and generate SourceMaps.

The code and the API is an (almost) direct transposition of the Javascript library developped by Mozilla (https://github.com/mozilla/source-map/). The current code is up to date to the commit #d55e947a5845c94a8781bc82b456d1f362ce98b2.

# Installation

The library is available in the Maven Central repository at the coordinates `org.hibnet` - `jsourcemap`: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.hibnet%22%20a%3A%22jsourcemap%22

# Development

## publish snapshots

- run `ant publish-snapshot`

## delete snapshots:

- go to https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage~/org/hibnet/
- delete folder

## release:

- run `ant release`
- go to https://oss.sonatype.org/#stagingRepositories
- select the orghibnet-XXXX repo and click 'Close'
- wait for validation, refresh, and then click 'Release'
