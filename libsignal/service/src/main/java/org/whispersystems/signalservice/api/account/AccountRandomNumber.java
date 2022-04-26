package org.whispersystems.signalservice.api.account;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountRandomNumber {
  @JsonProperty
  private String number;

  @JsonProperty
  private String code;

  public AccountRandomNumber(){

  }

  public AccountRandomNumber(String number,String code){
    this.code = code;
    this.number = number;
  }

  public String getNumber(){
    return number;
  }

  public void setNumber(String number){
    this.number = number;
  }


  public String getCode(){
    return code;
  }

  public void setCode(String code){
    this.code = code;
  }
}
