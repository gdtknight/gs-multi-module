# Creating a Multi Module Project - spring.io

## 계기
Multi Module Project 실습 강의를 듣던 중 문제가 발생했다.<br>
강의에서는 IntelliJ 를 사용하는데, 나는 SSH Terminal 을 사용해서 실습을 진행하는 상황..!!<br>

## 발생한 문제점
IntelliJ 에서는 Module 생성과 설정이 자동화가 되어 있는데 반해 <br>
나는 lunarVim(IDE에 가장 근접한..vim이라고나 할까) + java-debug + vscode-java-test 환경이라 <br>
어지간한 작업은 다 수동 설정이 필요하다. <br>

## 해결과정 1단계
Multi Module 생성을 해본적이 없어 개별 Module 단위로 Project를 생성하고 같은 폴더에 집어넣어봤다.<br>
역시 예상대로 정상적인 작동을 하지 않았고 build.gradle 설정을 하면 될거라는 생각에,<br>
gradle HomePage에서 관련된 내용을 검색해보고 buildSrc 를 비롯하여 각 module의 build.gradle 파일을 <br>
sample project와 동일하게 설정해주고 진행해보았다. 그리고 마찬가지로 Spring Boot Dependency 도 추가했는데 <br>
build 자체가 되지 않는 문제가 발생했다.

## 해결과정 2단계
`gradle CLI` 를 통해 multi module project 생성은 간단했는데 문제는 Spring Boot Dependency를 추가하는 순간 발생했다.<br>
nvim-jdtls plugin 을 통해 project build, debug 모두 안되는 것은 물론 LSP 상에도 error Log만 줄줄이 찍혔다.
Spring Boot 의존성을 추가할 때 해결과정 1단계에서는 buildSrc에 공통 convention.gradle 파일을 두고<br>
그 파일 안에 Spring Boot plugin 을 추가해뒀었다. 이 때 생각하기를 공통 convention이 개별 모듈에 <br>
제대로 적용이 되지 않는게 아닐까 생각하고 각 모듈 디렉토리에 존재하는 build.gradle 에 각각 Spring Boot plugin을 집어넣고<br>
다시 한 번 빌드를 시도해 봤으나 여전히 되지 않았다.

## 최종 해결 및 정리
다행스럽게도 [Creating a Multi Module Project](https://spring.io/guides/gs/multi-module/) Guide를 찾을 수 있었다.
해당 프로젝트를 단계별로 따라하다 보니 문제점을 찾을 수 있었는데,<br>

1. `spring-boot-gradle-plugin`은 build 과정에서 `bootJar` task를 수행한다.
2. `bootJar`는 `executable jar` 생성을 담당하고, 생성 후에 `main method`를 요청한다. (원문에서 request라고 표현되어 있다.)
3. 실습 과정에서 생성했던 하나의 module은 library 역할이기에 `main method`가 따로 존재하지 않는다.
4. 이로 인해 오류가 발생한다.

해당 가이드에서 나와 있는 방법은 다음과 같다.

1. 라이브러리 모듈 내 `build.gradle` 파일 내에 `spring-boot-gradle-plugin` 에 `apply false` 를 추가한다.
2. Spring Boot dependency management를 위해 dependencyManagement 를 따로 추가한다.

다음은 `build.gradle`의 내용이다
```groovy
plugins {
  id 'org.springframework.boot' version '2.5.2' apply false
  id 'io.spring.dependency-management' version '1.0.11.RELEASE'
  // ... other plugins
}

dependencyManagement {
  imports {
    mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
  }
}
```

### 마무리
문제가 발생했을 때 LSP error log 에 찍힌 내용은 저것보다 훨씬 방대했다. log를 통해 확인한 바로는<br>
library module 에 적용한 spring-boot-gradle-plugin 이 제대로 적용되지 않는다는 내용이였는데<br>
아마 bootJar task 수행 과정에서 발생한 에러일 것이라 짐작하고 있다. 명시적으로 해당 task가 언급되어 있지 않아서<br>
생각보다 간단한 내용임에도 이틀이란 시간을 투자해서 겨우 해결할 수 있었다.<br>

이 과정에서 nvim-jdtls 플러그인이 문제인가 싶어 해당 github issue에서도 한참을 뒤적거렸다.<br>
덕분에 jdtls 플러그인에서 제대로 적용되지 않고 있던 몇가지 명령어를 해결하는 방법도 알아냈고<br>
이를 통해 eclipse IDE 에서 project configuration 을 새로고침하는 등의 기능들을<br>
lunarvim 에서 사용하는 법도 알 수 있었다. debug session 연결을 위한 몇가지 명령어도 추가적으로<br>
알게 되었다.

향로님이 2017년에 작성하신 gradle multi module project 설정글도 볼 수 있었는데<br>
개발 공부를 왜 공식 문서로 해야하는지 몸으로 깨달을 수 있었다.<br>

gradle 도 버전마다 build script 설정하는 방법이 바뀔 때도 있고 버전업이 되면서 하위호환성 문제에<br>
gradle java compability problem 도 발생할 수 있는데, 공식 문서를 자세히 보다보면 답이 나이있는 경우가 많다<br>
spring의 경우도 마찬가지였고<br>

처음이라 익숙하지 않아서 많은 시간이 걸렸지만 많은 부분을 깨달을 수 있었다.
