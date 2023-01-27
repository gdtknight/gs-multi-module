package com.example.multimodule.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.properties 를 src/main/resources/ 아래에 추가할 수 있지만
 * 이렇게 할 경우 차후에 library 를 이용하는 application에서 application.properties를 이용할 경우
 * 충돌이 발생할 가능성이 있다. 따라서 개발 시점에는 src/main/resources/ 디렉토리에 있는
 * application.properties를 이용하고 build 시에는 해당 파일을 제외하도록 build tool을 설정항여야 한다.
 */

@ConfigurationProperties("service")
public class ServiceProperties {

  /**
   * A message for the service.
   */
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

