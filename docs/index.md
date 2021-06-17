# Charybdis

Charybdis is a REST Server that provides convenience methods for accessing video and annotation data from M3/VARS. 

## API

Calls to the API return the annotations that match the query and information about media that have those annotations. 

### Query by Concept

Search for annotations and media by a concept. Concepts are identifiers, like species names or pieces of equipment [defined in the VARS knowledebase](http://m3.shore.mbari.org/kb/v1/concept)

`http://m3.shore.mbari.org/references/query/concept/{concept}`

Example:

<http://m3.shore.mbari.org/references/query/concept/Pandalus%20platyceros>

### Query by dive

Search for annotations and media by a deployment id (e.g. Ventana 1234)

`http://m3.shore.mbari.org/references/query/dive/{dive}`

Example:

<http://m3.shore.mbari.org/references/query/dive/Ventana%201234>

### Query by video file name

Search for annotations and media by a video files name (e.g. D1153_20190615T144237Z_prores.mov)

`http://m3.shore.mbari.org/references/query/file/{filename}`

Example without annotations: 

<http://m3.shore.mbari.org/references/query/file/D1153_20190615T144237Z_prores.mov>

Example with annotations:

<http://m3.shore.mbari.org/references/query/file/D1153_20190615T144237Z_prores.mov>

## Static-ish datasets

These endpoints provide datasets that were used for publications. Note that this data is "live"; changes/updates to annotations in VARS over time will be reflected in this data.

### [Revealing enigmatic mucus structures in the deep sea using DeepPIV](https://www.nature.com/articles/s41586-020-2345-2)

<http://m3.shore.mbari.org/references/n0>

The static data set can be found at <https://data.mbari.org/products/vars/references/Kakani_Nature20190609559.json>