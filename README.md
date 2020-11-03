# Jatex

[![](https://img.shields.io/github/issues/uhoefel/utils?style=flat-square)](https://github.com/uhoefel/utils/issues)
[![](https://img.shields.io/github/stars/uhoefel/utils?style=flat-square)](https://github.com/uhoefel/utils/stargazers)

[comment]: # [![DOI](https://zenodo.org/badge/308012469.svg)](https://zenodo.org/badge/latestdoi/308012469)
[![](https://img.shields.io/github/license/uhoefel/utils?style=flat-square)](https://choosealicense.com/licenses/mit/)

Utils is a small Java module designed to provide a number of convenience methods for regular expressions, mathematical operations, operations on strings, etc.

Some examples:
```java
Maths.isLong("123L");
Maths.compensatedSum(1e100, 1e-100, 1, 2e-18);
Strings.ordinalNumeral(3);
Types.canBeWidened(float.class, 3L);
```

Installation
============

The artifact can be found at maven central:
```xml
<dependency>
    <groupId>eu.hoefel</groupId>
    <artifactId>utils</artifactId>
    <version>0.1.0</version>
</dependency>
```

Requirements
============
Utils is designed to work with Java 15+. It needs preview-features enabled.