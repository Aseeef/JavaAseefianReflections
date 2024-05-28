<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Download][download-shield]][download-url]
[![Jitpack][jitpack-shield]][jitpack-url]
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">

<h3 align="center">Aseefian Aseefian Reflections [JAR]</h3>

  <p align="center">
    A Comprehensive Reflections Library for Java
    <br />
    <a href="https://github.com/Aseeef/AseefianProxyPool/wiki"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/Aseeef/AseefianProxyPool/issues">Report Bug</a>
    ·
    <a href="https://github.com/Aseeef/AseefianProxyPool/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
        <ul>
         <li><a href="#features">Features</a></li>
        </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
         <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

J.A.R. is a easy to use, convenient, fast, and battle tested Java Reflections library originally developed for use with in the Minecraft Plugin development scene. J.A.R. aims to make working with Java reflections much easier and straightforward removing much of the boilerplating traditionally needed for vanilla reflections. The library interface is designed to be as intuitive as possible.

### Features

* Ability to search methods and fields by their types.
* Support for variable length arguments and primitive to boxed conversions.
* Uses cacheing for fast performance (optionally using Ben Manes Caffeine library).
* Battle-tested on production systems with added unit testing for addition confidence of correctness.
* Well-documented library API.
* Thread-safe.
* Written 100% in pure java with no additional libraries requirements.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

JAR requires at least Java 11 or higher.

### Installation

JAR may be installed either via the Jitpack maven repository or downloaded directly and then added as a dependency.

#### Gradle
```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    implementation ('com.github.Aseeef:JavaAseefianReflections:latest.release')
}
```
#### Maven
```
<repositories>
    ...
	<repository>
		 <id>jitpack.io</id>
		 <url>https://jitpack.io</url>
	</repository>
</repositories>
```
```
<dependency>
	<groupId>com.github.Aseeef</groupId>
	<artifactId>JavaAseefianReflections</artifactId>
	<version>LATEST</version>
</dependency>
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Example Usage

```java
    // configure the JarConfig
    JARConfig config = new JARConfig();
            config.setAllowAccessingInheritedMethods(true);
            config.setAllowAccessingInheritedFields(true);
    // or leave the config parameter blank for the default config (which will work for most people)
    JavaAseefianReflections jar = JavaAseefianReflections.init(config);
    // Reflectively invoke some method - has a very natural taste almost like calling a method non-reflectively
    jar.invokeMethod(obj, "doSomething", param1, param2, param3)
```

_For more examples, please refer to the [Documentation](https://example.com)_

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- About the author -->
## Contact

Muhammad Aseef Imran -  [contact@aseef.dev](mail:contact@aseef.dev)

Project Link: [https://github.com/Aseeef/JavaAseefianReflections](https://github.com/Aseeef/JavaAseefianReflections)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/Aseeef/AseefianProxyPool.svg?style=for-the-badge
[contributors-url]: https://github.com/Aseeef/AseefianProxyPool/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Aseeef/AseefianProxyPool.svg?style=for-the-badge
[forks-url]: https://github.com/Aseeef/AseefianProxyPool/network/members
[stars-shield]: https://img.shields.io/github/stars/Aseeef/AseefianProxyPool.svg?style=for-the-badge
[stars-url]: https://github.com/Aseeef/AseefianProxyPool/stargazers
[issues-shield]: https://img.shields.io/github/issues/Aseeef/AseefianProxyPool.svg?style=for-the-badge
[issues-url]: https://github.com/Aseeef/AseefianProxyPool/issues
[license-shield]: https://img.shields.io/github/license/Aseeef/AseefianProxyPool.svg?style=for-the-badge
[license-url]: https://github.com/Aseeef/AseefianProxyPool/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/aseef/
[jitpack-shield]: https://img.shields.io/jitpack/version/com.github.Aseeef/AseefianProxyPool?style=for-the-badge
[jitpack-url]: https://jitpack.io/#Aseeef/AseefianProxyPool/
[download-shield]: https://img.shields.io/github/downloads/Aseeef/AseefianProxyPool/total?style=for-the-badge
[download-url]: https://github.com/Aseeef/AseefianProxyPool/releases
